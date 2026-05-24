package com.eventmacha.service;

import com.eventmacha.dto.request.CreateOrderRequest;
import com.eventmacha.dto.response.OrderResponse;
import com.eventmacha.entity.OrderEntity;
import com.eventmacha.entity.RateCardPlanEntity;
import com.eventmacha.exception.BusinessException;
import com.eventmacha.exception.NotFoundException;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.repository.OrderRepository;
import com.eventmacha.repository.RateCardRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.util.IdGenerator;
import com.eventmacha.util.TimeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Service for creating and retrieving orders.
 */
@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    @Inject
    OrderRepository orderRepository;

    @Inject
    RateCardRepository rateCardRepository;

    @Inject
    AuthenticatedUserContext userContext;

    /**
     * Create a new order for the authenticated user.
     *
     * <p>Validates that the rate card and plan exist and are active.
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        AuthenticatedUser principal = requireAuthenticated();

        // Validate rate card exists
        rateCardRepository.findRateCardById(request.getRateCardId())
                .filter(rc -> Boolean.TRUE.equals(rc.getActive()))
                .orElseThrow(() -> new NotFoundException("RateCard", request.getRateCardId()));

        // Validate plan exists and is active
        RateCardPlanEntity plan = rateCardRepository
                .findPlan(request.getRateCardId(), request.getPlanType())
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new NotFoundException("RateCardPlan",
                        request.getRateCardId() + "/" + request.getPlanType()));

        OrderEntity order = new OrderEntity();
        order.setOrderId(IdGenerator.orderId());
        order.setUserId(principal.getUserId() != null ? principal.getUserId() : principal.getCognitoUserId());
        order.setUserType("CUSTOMER");
        order.setRateCardId(request.getRateCardId());
        order.setPlanType(request.getPlanType());
        order.setBasePrice(plan.getBasePrice());
        order.setDiscount(plan.getDiscount());
        order.setFinalPrice(plan.getFinalPrice());
        order.setOrderStatus("PENDING");
        order.setEventId(request.getEventId());
        order.setCreatedAt(TimeUtil.now());
        order.setUpdatedAt(TimeUtil.now());

        orderRepository.save(order);
        LOG.infof("Order created: %s for user: %s", order.getOrderId(), order.getUserId());

        return toOrderResponse(order);
    }

    /**
     * Get an order by ID.
     * Users may only view their own orders (unless ADMIN).
     */
    public OrderResponse getOrder(String orderId) {
        AuthenticatedUser principal = requireAuthenticated();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        // Ownership check
        String userId = principal.getUserId() != null ? principal.getUserId() : principal.getCognitoUserId();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("ACCESS_DENIED", "You do not have access to this order");
        }

        return toOrderResponse(order);
    }

    /**
     * Get all orders for the authenticated user.
     */
    public List<OrderResponse> getOrdersForCurrentUser() {
        AuthenticatedUser principal = requireAuthenticated();
        String userId = principal.getUserId() != null ? principal.getUserId() : principal.getCognitoUserId();
        return orderRepository.findByUserId(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    // ── Internal access ───────────────────────────────────────────────────────

    public OrderEntity findEntityById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthenticatedUser requireAuthenticated() {
        AuthenticatedUser user = userContext.get();
        if (user == null) throw new UnauthorizedException("Authentication required");
        return user;
    }

    private OrderResponse toOrderResponse(OrderEntity e) {
        OrderResponse r = new OrderResponse();
        r.setOrderId(e.getOrderId());
        r.setUserId(e.getUserId());
        r.setUserType(e.getUserType());
        r.setRateCardId(e.getRateCardId());
        r.setPlanType(e.getPlanType());
        r.setBasePrice(e.getBasePrice());
        r.setDiscount(e.getDiscount());
        r.setFinalPrice(e.getFinalPrice());
        r.setOrderStatus(e.getOrderStatus());
        r.setEventId(e.getEventId());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
