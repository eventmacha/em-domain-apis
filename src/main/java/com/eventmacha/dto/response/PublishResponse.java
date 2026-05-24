package com.eventmacha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Publish state response DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublishResponse {

    private String orderId;
    private String publishVersionId;
    private Integer publishCount;
    private String status;
    private Long expiresAt;
    private String previewToken;
    private Long previewGeneratedAt;

    // Draft content (returned as raw JSON objects, not strings)
    private Object draftSiteConfig;
    private Object draftContentEn;
    private Object draftContentLocalized;
    private List<String> draftImages;

    // Published content
    private Object publishedSiteConfig;
    private Object publishedContentEn;
    private Object publishedContentLocalized;
    private List<String> publishedImages;

    private Long publishedAt;
    private Long createdAt;
    private Long updatedAt;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPublishVersionId() { return publishVersionId; }
    public void setPublishVersionId(String publishVersionId) { this.publishVersionId = publishVersionId; }

    public Integer getPublishCount() { return publishCount; }
    public void setPublishCount(Integer publishCount) { this.publishCount = publishCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    public String getPreviewToken() { return previewToken; }
    public void setPreviewToken(String previewToken) { this.previewToken = previewToken; }

    public Long getPreviewGeneratedAt() { return previewGeneratedAt; }
    public void setPreviewGeneratedAt(Long previewGeneratedAt) { this.previewGeneratedAt = previewGeneratedAt; }

    public Object getDraftSiteConfig() { return draftSiteConfig; }
    public void setDraftSiteConfig(Object draftSiteConfig) { this.draftSiteConfig = draftSiteConfig; }

    public Object getDraftContentEn() { return draftContentEn; }
    public void setDraftContentEn(Object draftContentEn) { this.draftContentEn = draftContentEn; }

    public Object getDraftContentLocalized() { return draftContentLocalized; }
    public void setDraftContentLocalized(Object draftContentLocalized) { this.draftContentLocalized = draftContentLocalized; }

    public List<String> getDraftImages() { return draftImages; }
    public void setDraftImages(List<String> draftImages) { this.draftImages = draftImages; }

    public Object getPublishedSiteConfig() { return publishedSiteConfig; }
    public void setPublishedSiteConfig(Object publishedSiteConfig) { this.publishedSiteConfig = publishedSiteConfig; }

    public Object getPublishedContentEn() { return publishedContentEn; }
    public void setPublishedContentEn(Object publishedContentEn) { this.publishedContentEn = publishedContentEn; }

    public Object getPublishedContentLocalized() { return publishedContentLocalized; }
    public void setPublishedContentLocalized(Object publishedContentLocalized) { this.publishedContentLocalized = publishedContentLocalized; }

    public List<String> getPublishedImages() { return publishedImages; }
    public void setPublishedImages(List<String> publishedImages) { this.publishedImages = publishedImages; }

    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
