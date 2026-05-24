package com.eventmacha.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * DynamoDB entity for the RateCards table.
 *
 * <pre>
 * PK:     rateCardId
 * GSIs:
 *   userType-index – PK: userType
 * </pre>
 */
@DynamoDbBean
public class RateCardEntity {

    private String rateCardId;
    private String userType;
    private String rateCardType;
    private String currency;
    private Boolean active;
    private Long effectiveFrom;
    private Long effectiveTo;
    private Long createdAt;
    private Long updatedAt;

    @DynamoDbPartitionKey
    public String getRateCardId() { return rateCardId; }
    public void setRateCardId(String rateCardId) { this.rateCardId = rateCardId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "userType-index")
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getRateCardType() { return rateCardType; }
    public void setRateCardType(String rateCardType) { this.rateCardType = rateCardType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @DynamoDbAttribute("effectiveFrom")
    public Long getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Long effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    @DynamoDbAttribute("effectiveTo")
    public Long getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Long effectiveTo) { this.effectiveTo = effectiveTo; }

    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("updatedAt")
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
