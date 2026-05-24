package com.eventmacha.dto.response;

import com.eventmacha.enums.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * User profile response DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String userId;
    private UserType userType;
    private String email;
    private String phone;
    private String fullName;
    private String profileImage;
    private String authProvider;
    private String status;
    private Long createdAt;
    private Long updatedAt;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}