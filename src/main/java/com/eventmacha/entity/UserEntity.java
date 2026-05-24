package com.eventmacha.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

/**
 * DynamoDB entity for the Users table.
 *
 * <pre>
 * PK:      userId
 * GSIs:
 *   email-index     – PK: email
 *   userType-index  – PK: userType
 *   provider-index  – PK: authProvider, SK: providerUserId
 * </pre>
 */
@DynamoDbBean
public class UserEntity {

    private String userId;
    private String userType;
    private String email;
    private String phone;
    private String fullName;
    private String profileImage;
    private String authProvider;
    private String providerUserId;
    private String cognitoUserId;
    private String status;
    private Long createdAt;
    private Long updatedAt;

    // ── PK ────────────────────────────────────────────────────────────────────

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // ── GSI: email-index ─────────────────────────────────────────────────────

    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // ── GSI: userType-index ───────────────────────────────────────────────────

    @DynamoDbSecondaryPartitionKey(indexNames = "userType-index")
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    // ── GSI: provider-index ───────────────────────────────────────────────────

    @DynamoDbSecondaryPartitionKey(indexNames = "provider-index")
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    @DynamoDbSecondarySortKey(indexNames = "provider-index")
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    // ── Regular attributes ────────────────────────────────────────────────────

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getCognitoUserId() { return cognitoUserId; }
    public void setCognitoUserId(String cognitoUserId) { this.cognitoUserId = cognitoUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("updatedAt")
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
