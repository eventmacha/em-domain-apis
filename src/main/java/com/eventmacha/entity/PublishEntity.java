package com.eventmacha.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;

/**
 * DynamoDB entity for the Publish table.
 *
 * <pre>
 * PK: orderId
 *
 * JSON String fields:  draftSiteConfig, draftContentEn, draftContentLocalized,
 *                      publishedSiteConfig, publishedContentEn, publishedContentLocalized
 * List&lt;String&gt; fields: draftImages, publishedImages
 * </pre>
 */
@DynamoDbBean
public class PublishEntity {

    private String orderId;
    private String publishVersionId;
    private Integer publishCount;
    private String status;
    private Long expiresAt;

    private String previewToken;
    private Long previewGeneratedAt;

    // Draft content (stored as JSON String)
    private String draftSiteConfig;
    private String draftContentEn;
    private String draftContentLocalized;
    private List<String> draftImages;

    // Published content (stored as JSON String)
    private String publishedSiteConfig;
    private String publishedContentEn;
    private String publishedContentLocalized;
    private List<String> publishedImages;

    private Long publishedAt;
    private Long createdAt;
    private Long updatedAt;

    @DynamoDbPartitionKey
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPublishVersionId() { return publishVersionId; }
    public void setPublishVersionId(String publishVersionId) { this.publishVersionId = publishVersionId; }

    public Integer getPublishCount() { return publishCount; }
    public void setPublishCount(Integer publishCount) { this.publishCount = publishCount; }

    /** Status: DRAFT | PREVIEW | PUBLISHED | EXPIRED | ARCHIVED */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @DynamoDbAttribute("expiresAt")
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    public String getPreviewToken() { return previewToken; }
    public void setPreviewToken(String previewToken) { this.previewToken = previewToken; }

    @DynamoDbAttribute("previewGeneratedAt")
    public Long getPreviewGeneratedAt() { return previewGeneratedAt; }
    public void setPreviewGeneratedAt(Long previewGeneratedAt) { this.previewGeneratedAt = previewGeneratedAt; }

    /** JSON String – draft site config object. */
    public String getDraftSiteConfig() { return draftSiteConfig; }
    public void setDraftSiteConfig(String draftSiteConfig) { this.draftSiteConfig = draftSiteConfig; }

    /** JSON String – draft English content. */
    public String getDraftContentEn() { return draftContentEn; }
    public void setDraftContentEn(String draftContentEn) { this.draftContentEn = draftContentEn; }

    /** JSON String – draft localized content. */
    public String getDraftContentLocalized() { return draftContentLocalized; }
    public void setDraftContentLocalized(String draftContentLocalized) { this.draftContentLocalized = draftContentLocalized; }

    /** List of image URLs / keys for draft. */
    public List<String> getDraftImages() { return draftImages; }
    public void setDraftImages(List<String> draftImages) { this.draftImages = draftImages; }

    /** JSON String – published site config object. */
    public String getPublishedSiteConfig() { return publishedSiteConfig; }
    public void setPublishedSiteConfig(String publishedSiteConfig) { this.publishedSiteConfig = publishedSiteConfig; }

    /** JSON String – published English content. */
    public String getPublishedContentEn() { return publishedContentEn; }
    public void setPublishedContentEn(String publishedContentEn) { this.publishedContentEn = publishedContentEn; }

    /** JSON String – published localized content. */
    public String getPublishedContentLocalized() { return publishedContentLocalized; }
    public void setPublishedContentLocalized(String publishedContentLocalized) { this.publishedContentLocalized = publishedContentLocalized; }

    /** List of image URLs / keys for published state. */
    public List<String> getPublishedImages() { return publishedImages; }
    public void setPublishedImages(List<String> publishedImages) { this.publishedImages = publishedImages; }

    @DynamoDbAttribute("publishedAt")
    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }

    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("updatedAt")
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
