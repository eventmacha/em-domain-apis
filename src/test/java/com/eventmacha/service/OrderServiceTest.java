package com.eventmacha.service;

import com.eventmacha.dto.request.CreateOrderRequest;
import com.eventmacha.dto.response.OrderResponse;
import com.eventmacha.entity.OrderEntity;
import com.eventmacha.entity.RateCardEntity;
import com.eventmacha.entity.RateCardPlanEntity;
import com.eventmacha.exception.BusinessException;
import com.eventmacha.exception.NotFoundException;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.repository.OrderRepository;
import com.eventmacha.repository.RateCardRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    RateCardRepository rateCardRepository;

    @Mock
    AuthenticatedUserContext userContext;

    @InjectMocks
    OrderService orderService;

    private AuthenticatedUser principal;
    private RateCardEntity rateCard;
    private RateCardPlanEntity plan;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUser("cognito-sub", "user@test.com", "user-001", "GOOGLE");

        rateCard = new RateCardEntity();
        rateCard.setRateCardId("rc-001");
        rateCard.setUserType("CUSTOMER");
        rateCard.setActive(true);

        plan = new RateCardPlanEntity();
        plan.setRateCardId("rc-001");
        plan.setPlanType("BASIC");
        plan.setBasePrice(499.0);
        plan.setDiscount(0.0);
        plan.setFinalPrice(499.0);
        plan.setActive(true);
    }

    // ── createOrder ───────────────────────────────────────────────────────────

    @Test
    void createOrder_validRequest_createsOrder() {
        when(userContext.get()).thenReturn(principal);
        when(rateCardRepository.findRateCardById("rc-001")).thenReturn(Optional.of(rateCard));
        when(rateCardRepository.findPlan("rc-001", "BASIC")).thenReturn(Optional.of(plan));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setRateCardId("rc-001");
        request.setPlanType("BASIC");

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response.getOrderId());
        assertEquals("PENDING", response.getOrderStatus());
        assertEquals(499.0, response.getFinalPrice());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void createOrder_rateCardNotFound_throwsNotFound() {
        when(userContext.get()).thenReturn(principal);
        when(rateCardRepository.findRateCardById("unknown")).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setRateCardId("unknown");
        request.setPlanType("BASIC");

        assertThrows(NotFoundException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_inactivePlan_throwsNotFound() {
        plan.setActive(false);
        when(userContext.get()).thenReturn(principal);
        when(rateCardRepository.findRateCardById("rc-001")).thenReturn(Optional.of(rateCard));
        when(rateCardRepository.findPlan("rc-001", "BASIC")).thenReturn(Optional.of(plan));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setRateCardId("rc-001");
        request.setPlanType("BASIC");

        assertThrows(NotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_notAuthenticated_throwsUnauthorized() {
        when(userContext.get()).thenReturn(null);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setRateCardId("rc-001");
        request.setPlanType("BASIC");

        assertThrows(UnauthorizedException.class, () -> orderService.createOrder(request));
    }

    // ── getOrder ──────────────────────────────────────────────────────────────

    @Test
    void getOrder_ownerAccess_returnsOrder() {
        OrderEntity order = new OrderEntity();
        order.setOrderId("ord-001");
        order.setUserId("user-001");
        order.setOrderStatus("PENDING");
        order.setCreatedAt(System.currentTimeMillis());

        when(userContext.get()).thenReturn(principal);
        when(orderRepository.findById("ord-001")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder("ord-001");
        assertEquals("ord-001", response.getOrderId());
    }

    @Test
    void getOrder_differentUser_throwsBusinessException() {
        OrderEntity order = new OrderEntity();
        order.setOrderId("ord-001");
        order.setUserId("different-user");
        order.setCreatedAt(System.currentTimeMillis());

        when(userContext.get()).thenReturn(principal);
        when(orderRepository.findById("ord-001")).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.getOrder("ord-001"));
    }
}
