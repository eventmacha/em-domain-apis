package com.eventmacha.service;

import com.eventmacha.client.RazorpayClient;
import com.eventmacha.dto.request.CreatePaymentRequest;
import com.eventmacha.dto.response.PaymentResponse;
import com.eventmacha.entity.OrderEntity;
import com.eventmacha.entity.PaymentEntity;
import com.eventmacha.exception.BusinessException;
import com.eventmacha.repository.PaymentHistoryRepository;
import com.eventmacha.repository.PaymentRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    RazorpayClient razorpayClient;

    @Mock
    OrderService orderService;

    @Mock
    AuthenticatedUserContext userContext;

    @Mock
    JsonUtil jsonUtil;

    @InjectMocks
    PaymentService paymentService;

    private AuthenticatedUser principal;
    private OrderEntity order;

    @BeforeEach
    void setUp() throws Exception {
        principal = new AuthenticatedUser("cognito-sub", "user@test.com", "user-001", "GOOGLE");

        order = new OrderEntity();
        order.setOrderId("ord-001");
        order.setUserId("user-001");
        order.setOrderStatus("PENDING");
        order.setFinalPrice(499.0);
        order.setCreatedAt(System.currentTimeMillis());
    }

    // ── createPayment ─────────────────────────────────────────────────────────

    @Test
    void createPayment_validOrder_initiatesPayment() throws Exception {
        when(userContext.get()).thenReturn(principal);
        when(orderService.findEntityById("ord-001")).thenReturn(order);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rzpResponse = mapper.readTree("{\"id\":\"order_rzp_123\",\"amount\":49900}");
        when(razorpayClient.createOrder(49900L, "INR", "ord-001")).thenReturn(rzpResponse);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("ord-001");

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response.getPaymentId());
        assertEquals("INITIATED", response.getPaymentStatus());
        assertEquals("order_rzp_123", response.getRazorpayOrderId());
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
        verify(paymentHistoryRepository, times(1)).save(any());
    }

    @Test
    void createPayment_orderNotPending_throwsBusinessException() {
        order.setOrderStatus("COMPLETED");
        when(userContext.get()).thenReturn(principal);
        when(orderService.findEntityById("ord-001")).thenReturn(order);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("ord-001");

        assertThrows(BusinessException.class, () -> paymentService.createPayment(request));
        verify(razorpayClient, never()).createOrder(anyLong(), any(), any());
    }

    // ── handleWebhook ─────────────────────────────────────────────────────────

    @Test
    void handleWebhook_invalidSignature_throwsBusinessException() {
        when(razorpayClient.verifyWebhookSignature(any(), any())).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> paymentService.handleWebhook("{}", "bad-signature"));
    }

    @Test
    void handleWebhook_validCaptureEvent_updatesPaymentStatus() throws Exception {
        String rawBody = """
                {
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_rzp_001",
                        "order_id": "order_rzp_123",
                        "method": "upi"
                      }
                    }
                  }
                }
                """;

        PaymentEntity existingPayment = new PaymentEntity();
        existingPayment.setPaymentId("pay-001");
        existingPayment.setGatewayOrderId("order_rzp_123");
        existingPayment.setPaymentStatus("INITIATED");
        existingPayment.setCreatedAt(System.currentTimeMillis());

        when(razorpayClient.verifyWebhookSignature(rawBody, "valid-sig")).thenReturn(true);
        when(jsonUtil.getMapper()).thenReturn(new ObjectMapper());
        when(paymentRepository.findByOrderId("order_rzp_123")).thenReturn(List.of(existingPayment));

        paymentService.handleWebhook(rawBody, "valid-sig");

        verify(paymentRepository, times(1)).save(argThat(p -> "CAPTURED".equals(p.getPaymentStatus())));
        verify(paymentHistoryRepository, times(1)).save(any());
    }
}
