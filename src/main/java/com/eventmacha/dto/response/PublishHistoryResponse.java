package com.eventmacha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Publish history entry response DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublishHistoryResponse {

    private String orderId;
    private String publishVersionId;
    private Long expiresAt;
    private Object siteConfig;
    private Object contentEn;
    private Object contentLocalized;
    private Object images;
    private String publishedBy;
    private String status;
    private String changeReason;
    private Long publishedAt;
    private Long createdAt;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPublishVersionId() { return publishVersionId; }
    public void setPublishVersionId(String publishVersionId) { this.publishVersionId = publishVersionId; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    public Object getSiteConfig() { return siteConfig; }
    public void setSiteConfig(Object siteConfig) { this.siteConfig = siteConfig; }

    public Object getContentEn() { return contentEn; }
    public void setContentEn(Object contentEn) { this.contentEn = contentEn; }

    public Object getContentLocalized() { return contentLocalized; }
    public void setContentLocalized(Object contentLocalized) { this.contentLocalized = contentLocalized; }

    public Object getImages() { return images; }
    public void setImages(Object images) { this.images = images; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
