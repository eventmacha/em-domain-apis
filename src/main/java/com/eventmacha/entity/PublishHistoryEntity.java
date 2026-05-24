package com.eventmacha.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB entity for the PublishHistory table.
 *
 * <pre>
 * PK: orderId
 * SK: publishVersionId
 *
 * Config / content stored as JSON String.
 * </pre>
 */
@DynamoDbBean
public class PublishHistoryEntity {

    private String orderId;
    private String publishVersionId;
    private Long expiresAt;
    private String siteConfig;       // JSON String
    private String contentEn;        // JSON String
    private String contentLocalized; // JSON String
    private String images;           // JSON String (serialized list)
    private String publishedBy;
    private String status;
    private String changeReason;
    private Long publishedAt;
    private Long createdAt;

    @DynamoDbPartitionKey
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @DynamoDbSortKey
    public String getPublishVersionId() { return publishVersionId; }
    public void setPublishVersionId(String publishVersionId) { this.publishVersionId = publishVersionId; }

    @DynamoDbAttribute("expiresAt")
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    /** Snapshot of the site configuration as a JSON string. */
    public String getSiteConfig() { return siteConfig; }
    public void setSiteConfig(String siteConfig) { this.siteConfig = siteConfig; }

    /** English content snapshot as a JSON string. */
    public String getContentEn() { return contentEn; }
    public void setContentEn(String contentEn) { this.contentEn = contentEn; }

    /** Localized content snapshot as a JSON string. */
    public String getContentLocalized() { return contentLocalized; }
    public void setContentLocalized(String contentLocalized) { this.contentLocalized = contentLocalized; }

    /** Image list serialized as a JSON string. */
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    @DynamoDbAttribute("publishedAt")
    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }

    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
