package com.eventmacha.service;

import com.eventmacha.dto.request.PublishLiveRequest;
import com.eventmacha.dto.request.SaveDraftRequest;
import com.eventmacha.dto.response.PublishResponse;
import com.eventmacha.entity.OrderEntity;
import com.eventmacha.entity.PublishEntity;
import com.eventmacha.exception.BusinessException;
import com.eventmacha.exception.NotFoundException;
import com.eventmacha.repository.OrderRepository;
import com.eventmacha.repository.PublishRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishServiceTest {

    @Mock
    PublishRepository publishRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    AuthenticatedUserContext userContext;

    @Mock
    JsonUtil jsonUtil;

    @InjectMocks
    PublishService publishService;

    private AuthenticatedUser principal;
    private OrderEntity order;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUser("cognito-sub", "user@test.com", "user-001", "GOOGLE");

        order = new OrderEntity();
        order.setOrderId("ord-001");
        order.setUserId("user-001");
        order.setCreatedAt(System.currentTimeMillis());
    }

    // ── saveDraft ─────────────────────────────────────────────────────────────

    @Test
    void saveDraft_newRecord_createsPublishEntity() {
        when(userContext.get()).thenReturn(principal);
        when(orderRepository.findById("ord-001")).thenReturn(Optional.of(order));
        when(publishRepository.findByOrderId("ord-001")).thenReturn(Optional.empty());
        when(jsonUtil.toJson(any())).thenReturn("{\"theme\":\"dark\"}");
        when(jsonUtil.fromJson(anyString(), eq(Object.class)))
                .thenReturn(Optional.of(Map.of("theme", "dark")));

        SaveDraftRequest request = new SaveDraftRequest();
        request.setOrderId("ord-001");
        request.setSiteConfig(Map.of("theme", "dark"));
        request.setImages(List.of("img1.jpg", "img2.jpg"));

        PublishResponse response = publishService.saveDraft(request);

        assertEquals("ord-001", response.getOrderId());
        assertEquals("DRAFT", response.getStatus());
        verify(publishRepository, times(1)).save(any(PublishEntity.class));
    }

    @Test
    void saveDraft_differentOwner_throwsBusinessException() {
        order.setUserId("other-user");
        when(userContext.get()).thenReturn(principal);
        when(orderRepository.findById("ord-001")).thenReturn(Optional.of(order));

        SaveDraftRequest request = new SaveDraftRequest();
        request.setOrderId("ord-001");

        assertThrows(BusinessException.class, () -> publishService.saveDraft(request));
    }

    // ── generatePreview ───────────────────────────────────────────────────────

    @Test
    void generatePreview_fromDraft_generatesToken() {
        PublishEntity entity = new PublishEntity();
        entity.setOrderId("ord-001");
        entity.setStatus("DRAFT");
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());

        when(userContext.get()).thenReturn(principal);
        when(publishRepository.findByOrderId("ord-001")).thenReturn(Optional.of(entity));

        PublishResponse response = publishService.generatePreview("ord-001");

        assertEquals("PREVIEW", response.getStatus());
        assertNotNull(response.getPreviewToken());
        verify(publishRepository, times(1)).save(any(PublishEntity.class));
    }

    @Test
    void generatePreview_noRecord_throwsNotFound() {
        when(userContext.get()).thenReturn(principal);
        when(publishRepository.findByOrderId("ord-001")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> publishService.generatePreview("ord-001"));
    }

    // ── publishLive ───────────────────────────────────────────────────────────

    @Test
    void publishLive_fromDraft_publishesAndCreatesHistory() {
        PublishEntity entity = new PublishEntity();
        entity.setOrderId("ord-001");
        entity.setStatus("DRAFT");
        entity.setDraftSiteConfig("{\"theme\":\"dark\"}");
        entity.setDraftContentEn("{\"title\":\"My Event\"}");
        entity.setPublishCount(0);
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());

        when(userContext.get()).thenReturn(principal);
        when(publishRepository.findByOrderId("ord-001")).thenReturn(Optional.of(entity));
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(jsonUtil.toJson(any())).thenReturn("[]");
        when(jsonUtil.fromJson(anyString(), eq(Object.class)))
                .thenReturn(Optional.of(Map.of()));

        PublishLiveRequest request = new PublishLiveRequest();
        request.setOrderId("ord-001");
        request.setChangeReason("Initial publish");

        PublishResponse response = publishService.publishLive(request);

        assertEquals("PUBLISHED", response.getStatus());
        assertNotNull(response.getPublishVersionId());
        assertEquals(1, response.getPublishCount());
        verify(publishRepository, times(1)).save(any(PublishEntity.class));
        verify(publishRepository, times(1)).saveHistory(any());
    }

    @Test
    void publishLive_noDraftContent_throwsBusinessException() {
        PublishEntity entity = new PublishEntity();
        entity.setOrderId("ord-001");
        entity.setStatus("DRAFT");
        // No draft content set
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());

        when(userContext.get()).thenReturn(principal);
        when(publishRepository.findByOrderId("ord-001")).thenReturn(Optional.of(entity));
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));

        PublishLiveRequest request = new PublishLiveRequest();
        request.setOrderId("ord-001");

        assertThrows(BusinessException.class, () -> publishService.publishLive(request));
    }
}
