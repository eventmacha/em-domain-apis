package com.eventmacha.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB entity for the PaymentHistory table.
 *
 * <pre>
 * PK: paymentId
 * SK: eventTime (Number / Long)
 * </pre>
 */
@DynamoDbBean
public class PaymentHistoryEntity {

    private String paymentId;
    private Long eventTime;
    private String oldStatus;
    private String newStatus;
    private String source;
    private String payload;   // Stored as JSON String
    private Long createdAt;

    @DynamoDbPartitionKey
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    @DynamoDbSortKey
    @DynamoDbAttribute("eventTime")
    public Long getEventTime() { return eventTime; }
    public void setEventTime(Long eventTime) { this.eventTime = eventTime; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    /** Source: WEBHOOK | SYSTEM | MANUAL */
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    /** Raw webhook/event payload stored as a JSON string. */
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
