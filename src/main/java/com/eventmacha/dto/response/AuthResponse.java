package com.eventmacha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Auth response DTO returned after POST /auth/social and POST /auth/login.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String userId;
    private boolean isNewUser;
    private UserResponse user;
    private String accessToken;
    private String idToken;

    public AuthResponse() {}

    public AuthResponse(String userId, boolean isNewUser, UserResponse user) {
        this.userId = userId;
        this.isNewUser = isNewUser;
        this.user = user;
    }

    public AuthResponse(String userId, boolean isNewUser, UserResponse user, String accessToken, String idToken) {
        this.userId = userId;
        this.isNewUser = isNewUser;
        this.user = user;
        this.accessToken = accessToken;
        this.idToken = idToken;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isNewUser() { return isNewUser; }
    public void setNewUser(boolean newUser) { this.isNewUser = newUser; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
