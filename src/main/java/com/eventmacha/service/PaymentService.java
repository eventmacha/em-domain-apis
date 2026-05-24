package com.eventmacha.service;

import com.eventmacha.client.RazorpayClient;
import com.eventmacha.dto.request.CreatePaymentRequest;
import com.eventmacha.dto.response.PaymentResponse;
import com.eventmacha.entity.OrderEntity;
import com.eventmacha.entity.PaymentEntity;
import com.eventmacha.entity.PaymentHistoryEntity;
import com.eventmacha.exception.BusinessException;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.repository.PaymentHistoryRepository;
import com.eventmacha.repository.PaymentRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.util.IdGenerator;
import com.eventmacha.util.JsonUtil;
import com.eventmacha.util.TimeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * Service handling payment creation and webhook-triggered state transitions.
 */
@ApplicationScoped
public class PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentService.class);
    private static final String GATEWAY = "RAZORPAY";

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    PaymentHistoryRepository paymentHistoryRepository;

    @Inject
    RazorpayClient razorpayClient;

    @Inject
    OrderService orderService;

    @Inject
    AuthenticatedUserContext userContext;

    @Inject
    JsonUtil jsonUtil;

    /**
     * Create a Razorpay order and record the payment intent in DynamoDB.
     *
     * @return payment response containing the Razorpay orderId for the checkout SDK
     */
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        AuthenticatedUser principal = requireAuthenticated();

        // Validate the order
        OrderEntity order = orderService.findEntityById(request.getOrderId());
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException("ORDER_NOT_PENDING",
                    "Order " + order.getOrderId() + " is not in PENDING state");
        }

        // Amount in paise (multiply by 100)
        double finalPrice = order.getFinalPrice() != null ? order.getFinalPrice() : 0.0;
        long amountPaise = Math.round(finalPrice * 100);

        // Create Razorpay order
        JsonNode rzpOrder = razorpayClient.createOrder(amountPaise, "INR", order.getOrderId());
        String rzpOrderId = rzpOrder.get("id").asText();

        String userId = principal.getUserId() != null ? principal.getUserId() : principal.getCognitoUserId();

        // Persist payment record
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(IdGenerator.paymentId());
        payment.setOrderId(order.getOrderId());
        payment.setUserId(userId);
        payment.setGateway(GATEWAY);
        payment.setGatewayOrderId(rzpOrderId);
        payment.setAmount(finalPrice);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus("INITIATED");
        payment.setCreatedAt(TimeUtil.now());
        payment.setUpdatedAt(TimeUtil.now());
        paymentRepository.save(payment);

        // Audit trail
        appendHistory(payment.getPaymentId(), null, "INITIATED", "SYSTEM", null);

        LOG.infof("Payment initiated: %s, razorpay_order=%s", payment.getPaymentId(), rzpOrderId);

        PaymentResponse response = toPaymentResponse(payment);
        response.setRazorpayOrderId(rzpOrderId);
        return response;
    }

    /**
     * Handle a Razorpay webhook event (payment.captured, payment.failed, etc.).
     *
     * @param rawBody   raw webhook payload string
     * @param signature X-Razorpay-Signature header value
     */
    public void handleWebhook(String rawBody, String signature) {
        // Verify signature first
        if (!razorpayClient.verifyWebhookSignature(rawBody, signature)) {
            throw new BusinessException("INVALID_SIGNATURE", "Razorpay webhook signature is invalid");
        }

        JsonNode payload;
        try {
            payload = jsonUtil.getMapper().readTree(rawBody);
        } catch (Exception e) {
            throw new BusinessException("INVALID_PAYLOAD", "Cannot parse webhook body");
        }

        String event = payload.path("event").asText();
        LOG.infof("Processing Razorpay webhook: %s", event);

        JsonNode paymentNode = payload.path("payload").path("payment").path("entity");
        if (paymentNode.isMissingNode()) {
            LOG.warnf("No payment entity in webhook event: %s", event);
            return;
        }

        String rzpPaymentId = paymentNode.path("id").asText();
        String rzpOrderId = paymentNode.path("order_id").asText();
        String method = paymentNode.path("method").asText();

        // Find payment by gateway order ID
        paymentRepository.findByOrderId(rzpOrderId).stream()
                .filter(p -> rzpOrderId.equals(p.getGatewayOrderId()))
                .findFirst()
                .ifPresent(payment -> {
                    String oldStatus = payment.getPaymentStatus();
                    String newStatus = mapEventToStatus(event);

                    payment.setGatewayPaymentId(rzpPaymentId);
                    payment.setPaymentMethod(method);
                    payment.setPaymentStatus(newStatus);
                    payment.setUpdatedAt(TimeUtil.now());

                    if ("CAPTURED".equals(newStatus)) {
                        payment.setCapturedAt(TimeUtil.now());
                    }

                    paymentRepository.save(payment);
                    appendHistory(payment.getPaymentId(), oldStatus, newStatus, "WEBHOOK", rawBody);
                    LOG.infof("Payment %s status updated: %s → %s", payment.getPaymentId(), oldStatus, newStatus);
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String mapEventToStatus(String event) {
        return switch (event) {
            case "payment.captured" -> "CAPTURED";
            case "payment.failed"   -> "FAILED";
            case "payment.authorized" -> "AUTHORIZED";
            case "refund.created"   -> "REFUNDED";
            default -> "UNKNOWN";
        };
    }

    private void appendHistory(String paymentId, String oldStatus, String newStatus,
                               String source, String rawPayload) {
        PaymentHistoryEntity history = new PaymentHistoryEntity();
        history.setPaymentId(paymentId);
        history.setEventTime(TimeUtil.now());
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setSource(source);
        history.setPayload(rawPayload);
        history.setCreatedAt(TimeUtil.now());
        paymentHistoryRepository.save(history);
    }

    private AuthenticatedUser requireAuthenticated() {
        AuthenticatedUser user = userContext.get();
        if (user == null) throw new UnauthorizedException("Authentication required");
        return user;
    }

    private PaymentResponse toPaymentResponse(PaymentEntity e) {
        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(e.getPaymentId());
        r.setOrderId(e.getOrderId());
        r.setUserId(e.getUserId());
        r.setGateway(e.getGateway());
        r.setGatewayOrderId(e.getGatewayOrderId());
        r.setGatewayPaymentId(e.getGatewayPaymentId());
        r.setAmount(e.getAmount());
        r.setPaymentMethod(e.getPaymentMethod());
        r.setPaymentStatus(e.getPaymentStatus());
        r.setCapturedAt(e.getCapturedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
