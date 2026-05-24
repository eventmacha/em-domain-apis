package com.eventmacha.service;

import com.eventmacha.dto.request.SocialAuthRequest;
import com.eventmacha.dto.response.AuthResponse;
import com.eventmacha.dto.response.UserResponse;
import com.eventmacha.entity.UserEntity;
import com.eventmacha.exception.NotFoundException;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.repository.UserRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.security.CognitoJwtValidator;
import com.eventmacha.util.IdGenerator;
import com.eventmacha.util.TimeUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Authentication service – validates Cognito JWTs and upserts the user profile in DynamoDB.
 */
@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    CognitoJwtValidator jwtValidator;

    @Inject
    UserRepository userRepository;

    @Inject
    AuthenticatedUserContext userContext;

    /**
     * Handle social / email login.
     *
     * <ol>
     *   <li>Validate the Cognito idToken.</li>
     *   <li>Derive authProvider and providerUserId from claims.</li>
     *   <li>Upsert the user in DynamoDB (create if not exists).</li>
     *   <li>Return user profile.</li>
     * </ol>
     */
    public AuthResponse socialLogin(SocialAuthRequest request) {
        // Validate the Cognito JWT
        JWTClaimsSet claims;
        try {
            claims = jwtValidator.validate(request.getIdToken());
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Cognito token");
        }

        String cognitoSub = claims.getSubject();
        String authProvider = request.getProvider();
        String providerUserId = jwtValidator.extractProviderUserId(claims);

        // Derive email from claims or request body
        String email = safeGetClaim(claims, "email");
        if (email == null) email = request.getEmail();

        // Look up existing user by provider
        Optional<UserEntity> existing = userRepository.findByProvider(authProvider, providerUserId);

        boolean isNewUser = false;
        UserEntity user;

        if (existing.isPresent()) {
            user = existing.get();
            LOG.debugf("Existing user login: %s", user.getUserId());
            // Update mutable fields
            user.setUpdatedAt(TimeUtil.now());
            if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            userRepository.save(user);
        } else {
            // Create new user
            isNewUser = true;
            user = new UserEntity();
            user.setUserId(IdGenerator.userId());
            user.setUserType("CUSTOMER");
            user.setEmail(email);
            user.setFullName(request.getFullName());
            user.setProfileImage(request.getProfileImage());
            user.setPhone(request.getPhone());
            user.setAuthProvider(authProvider);
            user.setProviderUserId(providerUserId);
            user.setCognitoUserId(cognitoSub);
            user.setStatus("ACTIVE");
            user.setCreatedAt(TimeUtil.now());
            user.setUpdatedAt(TimeUtil.now());
            userRepository.save(user);
            LOG.infof("New user registered: %s via %s", user.getUserId(), authProvider);
        }

        return new AuthResponse(user.getUserId(), isNewUser, toUserResponse(user));
    }

    /**
     * Return the profile for the currently authenticated user.
     */
    public UserResponse getCurrentUser() {
        AuthenticatedUser principal = requireAuthenticated();
        // Try to find by cognitoSub first via email
        return userRepository.findByEmail(principal.getEmail())
                .map(this::toUserResponse)
                .orElseThrow(() -> new NotFoundException("User", principal.getCognitoUserId()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthenticatedUser requireAuthenticated() {
        AuthenticatedUser user = userContext.get();
        if (user == null) throw new UnauthorizedException("Authentication required");
        return user;
    }

    private String safeGetClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getStringClaim(name);
        } catch (Exception e) {
            return null;
        }
    }

    private UserResponse toUserResponse(UserEntity entity) {
        UserResponse r = new UserResponse();
        r.setUserId(entity.getUserId());
        r.setUserType(entity.getUserType());
        r.setEmail(entity.getEmail());
        r.setPhone(entity.getPhone());
        r.setFullName(entity.getFullName());
        r.setProfileImage(entity.getProfileImage());
        r.setAuthProvider(entity.getAuthProvider());
        r.setStatus(entity.getStatus());
        r.setCreatedAt(entity.getCreatedAt());
        r.setUpdatedAt(entity.getUpdatedAt());
        return r;
    }
}
