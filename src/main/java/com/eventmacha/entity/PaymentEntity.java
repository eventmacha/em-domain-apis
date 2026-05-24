package com.eventmacha.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

/**
 * DynamoDB entity for the Payments table.
 *
 * <pre>
 * PK: paymentId
 * GSI: order-index         – PK: orderId
 * GSI: user-payment-index  – PK: userId, SK: createdAt (Number)
 * </pre>
 *
 * Note: createdAt is stored as Number (Long) per schema spec.
 */
@DynamoDbBean
public class PaymentEntity {

    private String paymentId;
    private String orderId;
    private String userId;
    private String gateway;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
    private Long capturedAt;
    private Long createdAt;
    private Long updatedAt;

    @DynamoDbPartitionKey
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "order-index")
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "user-payment-index")
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    /** createdAt acts as SK for user-payment-index. Stored as Number. */
    @DynamoDbSecondarySortKey(indexNames = "user-payment-index")
    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getGatewayOrderId() { return gatewayOrderId; }
    public void setGatewayOrderId(String gatewayOrderId) { this.gatewayOrderId = gatewayOrderId; }

    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    @DynamoDbAttribute("capturedAt")
    public Long getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Long capturedAt) { this.capturedAt = capturedAt; }

    @DynamoDbAttribute("updatedAt")
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
