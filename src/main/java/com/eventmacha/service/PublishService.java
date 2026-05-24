package com.eventmacha.service;

import com.eventmacha.dto.request.PublishLiveRequest;
import com.eventmacha.dto.request.SaveDraftRequest;
import com.eventmacha.dto.response.PublishHistoryResponse;
import com.eventmacha.dto.response.PublishResponse;
import com.eventmacha.entity.OrderEntity;
import com.eventmacha.entity.PublishEntity;
import com.eventmacha.entity.PublishHistoryEntity;
import com.eventmacha.exception.BusinessException;
import com.eventmacha.exception.NotFoundException;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.repository.OrderRepository;
import com.eventmacha.repository.PublishRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.util.IdGenerator;
import com.eventmacha.util.JsonUtil;
import com.eventmacha.util.TimeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Publish flow service.
 *
 * <pre>
 * DRAFT → PREVIEW → PUBLISHED (copied to PublishHistory)
 * </pre>
 */
@ApplicationScoped
public class PublishService {

    private static final Logger LOG = Logger.getLogger(PublishService.class);

    @Inject
    PublishRepository publishRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    AuthenticatedUserContext userContext;

    @Inject
    JsonUtil jsonUtil;

    // ── Save Draft ────────────────────────────────────────────────────────────

    /**
     * Save (or update) the draft content for an order.
     */
    public PublishResponse saveDraft(SaveDraftRequest request) {
        AuthenticatedUser principal = requireAuthenticated();
        validateOrderOwnership(request.getOrderId(), principal);

        PublishEntity entity = publishRepository.findByOrderId(request.getOrderId())
                .orElseGet(() -> createNewPublishEntity(request.getOrderId(), principal));

        entity.setDraftSiteConfig(jsonUtil.toJson(request.getSiteConfig()));
        entity.setDraftContentEn(jsonUtil.toJson(request.getContentEn()));
        entity.setDraftContentLocalized(jsonUtil.toJson(request.getContentLocalized()));
        entity.setDraftImages(request.getImages());
        entity.setStatus("DRAFT");
        entity.setUpdatedAt(TimeUtil.now());

        publishRepository.save(entity);
        LOG.infof("Draft saved for order: %s", request.getOrderId());
        return toPublishResponse(entity);
    }

    // ── Generate Preview ──────────────────────────────────────────────────────

    /**
     * Generate a preview token for the draft content.
     */
    public PublishResponse generatePreview(String orderId) {
        AuthenticatedUser principal = requireAuthenticated();

        PublishEntity entity = publishRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Publish record", orderId));

        validateOwnership(entity.getOrderId(), principal);

        if (!"DRAFT".equals(entity.getStatus()) && !"PREVIEW".equals(entity.getStatus())) {
            throw new BusinessException("INVALID_STATE",
                    "Cannot generate preview from status: " + entity.getStatus());
        }

        entity.setPreviewToken(UUID.randomUUID().toString());
        entity.setPreviewGeneratedAt(TimeUtil.now());
        entity.setStatus("PREVIEW");
        entity.setUpdatedAt(TimeUtil.now());

        publishRepository.save(entity);
        LOG.infof("Preview generated for order: %s token: %s", orderId, entity.getPreviewToken());
        return toPublishResponse(entity);
    }

    // ── Publish Live ──────────────────────────────────────────────────────────

    /**
     * Promote draft content to PUBLISHED and copy to PublishHistory.
     */
    public PublishResponse publishLive(PublishLiveRequest request) {
        AuthenticatedUser principal = requireAuthenticated();

        PublishEntity entity = publishRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Publish record", request.getOrderId()));

        validateOwnership(entity.getOrderId(), principal);

        if (entity.getDraftSiteConfig() == null && entity.getDraftContentEn() == null) {
            throw new BusinessException("NO_DRAFT", "No draft content to publish");
        }

        // Copy draft → published
        String versionId = IdGenerator.publishVersionId();
        entity.setPublishVersionId(versionId);
        entity.setPublishedSiteConfig(entity.getDraftSiteConfig());
        entity.setPublishedContentEn(entity.getDraftContentEn());
        entity.setPublishedContentLocalized(entity.getDraftContentLocalized());
        entity.setPublishedImages(entity.getDraftImages());
        entity.setStatus("PUBLISHED");
        entity.setPublishedAt(TimeUtil.now());
        entity.setPublishCount(
                entity.getPublishCount() == null ? 1 : entity.getPublishCount() + 1);
        entity.setUpdatedAt(TimeUtil.now());

        publishRepository.save(entity);

        // Copy to publish history
        PublishHistoryEntity history = new PublishHistoryEntity();
        history.setOrderId(entity.getOrderId());
        history.setPublishVersionId(versionId);
        history.setSiteConfig(entity.getPublishedSiteConfig());
        history.setContentEn(entity.getPublishedContentEn());
        history.setContentLocalized(entity.getPublishedContentLocalized());
        history.setImages(jsonUtil.toJson(entity.getPublishedImages()));
        history.setPublishedBy(principal.getUserId() != null
                ? principal.getUserId()
                : principal.getCognitoUserId());
        history.setStatus("PUBLISHED");
        history.setChangeReason(request.getChangeReason());
        history.setPublishedAt(entity.getPublishedAt());
        history.setCreatedAt(TimeUtil.now());
        publishRepository.saveHistory(history);

        LOG.infof("Published live: order=%s version=%s", request.getOrderId(), versionId);
        return toPublishResponse(entity);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public PublishResponse getPublish(String orderId) {
        requireAuthenticated();
        return publishRepository.findByOrderId(orderId)
                .map(this::toPublishResponse)
                .orElseThrow(() -> new NotFoundException("Publish record", orderId));
    }

    public List<PublishHistoryResponse> getPublishHistory(String orderId) {
        requireAuthenticated();
        return publishRepository.findHistoryByOrderId(orderId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PublishEntity createNewPublishEntity(String orderId, AuthenticatedUser principal) {
        PublishEntity e = new PublishEntity();
        e.setOrderId(orderId);
        e.setStatus("DRAFT");
        e.setPublishCount(0);
        e.setCreatedAt(TimeUtil.now());
        e.setUpdatedAt(TimeUtil.now());
        return e;
    }

    private void validateOrderOwnership(String orderId, AuthenticatedUser principal) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        validateOwnership(order.getUserId(), principal);
    }

    private void validateOwnership(String entityUserId, AuthenticatedUser principal) {
        String currentUserId = principal.getUserId() != null
                ? principal.getUserId()
                : principal.getCognitoUserId();
        if (!entityUserId.equals(currentUserId)) {
            throw new BusinessException("ACCESS_DENIED", "You do not own this resource");
        }
    }

    private AuthenticatedUser requireAuthenticated() {
        AuthenticatedUser user = userContext.get();
        if (user == null)
            throw new UnauthorizedException("Authentication required");
        return user;
    }

    private PublishResponse toPublishResponse(PublishEntity e) {
        PublishResponse r = new PublishResponse();
        r.setOrderId(e.getOrderId());
        r.setPublishVersionId(e.getPublishVersionId());
        r.setPublishCount(e.getPublishCount());
        r.setStatus(e.getStatus());
        r.setExpiresAt(e.getExpiresAt());
        r.setPreviewToken(e.getPreviewToken());
        r.setPreviewGeneratedAt(e.getPreviewGeneratedAt());
        r.setDraftSiteConfig(parseJson(e.getDraftSiteConfig()));
        r.setDraftContentEn(parseJson(e.getDraftContentEn()));
        r.setDraftContentLocalized(parseJson(e.getDraftContentLocalized()));
        r.setDraftImages(e.getDraftImages());
        r.setPublishedSiteConfig(parseJson(e.getPublishedSiteConfig()));
        r.setPublishedContentEn(parseJson(e.getPublishedContentEn()));
        r.setPublishedContentLocalized(parseJson(e.getPublishedContentLocalized()));
        r.setPublishedImages(e.getPublishedImages());
        r.setPublishedAt(e.getPublishedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    private PublishHistoryResponse toHistoryResponse(PublishHistoryEntity e) {
        PublishHistoryResponse r = new PublishHistoryResponse();
        r.setOrderId(e.getOrderId());
        r.setPublishVersionId(e.getPublishVersionId());
        r.setExpiresAt(e.getExpiresAt());
        r.setSiteConfig(parseJson(e.getSiteConfig()));
        r.setContentEn(parseJson(e.getContentEn()));
        r.setContentLocalized(parseJson(e.getContentLocalized()));
        r.setImages(parseJson(e.getImages()));
        r.setPublishedBy(e.getPublishedBy());
        r.setStatus(e.getStatus());
        r.setChangeReason(e.getChangeReason());
        r.setPublishedAt(e.getPublishedAt());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    private Object parseJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank())
            return null;
        return jsonUtil.fromJson(jsonStr, Object.class).orElse(jsonStr);
    }
}
