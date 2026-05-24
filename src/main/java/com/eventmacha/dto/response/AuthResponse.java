package com.eventmacha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Auth response DTO returned after POST /auth/social.
 * Contains the user profile. The Cognito JWT is passed through from the client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String userId;
    private boolean isNewUser;
    private UserResponse user;

    public AuthResponse() {}

    public AuthResponse(String userId, boolean isNewUser, UserResponse user) {
        this.userId = userId;
        this.isNewUser = isNewUser;
        this.user = user;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isNewUser() { return isNewUser; }
    public void setNewUser(boolean newUser) { isNewUser = newUser; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}
