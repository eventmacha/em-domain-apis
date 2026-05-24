package com.eventmacha.security;

/**
 * Holds the authenticated user's JWT claims for the current request.
 * Populated by {@link JwtSecurityFilter} and stored in a ThreadLocal via
 * {@link AuthenticatedUserContext}.
 */
public class AuthenticatedUser {

    private final String cognitoUserId;
    private final String email;
    private final String userId;         // DynamoDB userId (may be null before upsert)
    private final String authProvider;

    public AuthenticatedUser(String cognitoUserId, String email, String userId, String authProvider) {
        this.cognitoUserId = cognitoUserId;
        this.email = email;
        this.userId = userId;
        this.authProvider = authProvider;
    }

    public String getCognitoUserId() { return cognitoUserId; }
    public String getEmail() { return email; }
    public String getUserId() { return userId; }
    public String getAuthProvider() { return authProvider; }
}
