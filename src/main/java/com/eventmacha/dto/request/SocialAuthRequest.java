package com.eventmacha.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /auth/social.
 * The client authenticates via Cognito (Google/Apple/Email) and sends the resulting JWT here.
 */
public class SocialAuthRequest {

    @NotBlank(message = "provider is required")
    @Pattern(regexp = "GOOGLE|APPLE|EMAIL", message = "provider must be GOOGLE, APPLE, or EMAIL")
    private String provider;

    @NotBlank(message = "idToken is required")
    private String idToken;

    private String email;
    private String fullName;
    private String profileImage;
    private String phone;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
