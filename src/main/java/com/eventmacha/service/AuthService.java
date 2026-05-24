package com.eventmacha.service;

import com.eventmacha.dto.request.LoginRequest;
import com.eventmacha.dto.request.RegisterUserRequest;
import com.eventmacha.dto.request.SocialAuthRequest;
import com.eventmacha.dto.response.AuthResponse;
import com.eventmacha.dto.response.UserResponse;
import com.eventmacha.entity.UserEntity;
import com.eventmacha.enums.UserType;
import com.eventmacha.exception.NotFoundException;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.exception.UserRegistrationException;
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
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import java.util.Optional;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    CognitoJwtValidator jwtValidator;

    @Inject
    UserRepository userRepository;

    @Inject
    AuthenticatedUserContext userContext;

    @Inject
    CognitoService cognitoService;

    public AuthResponse registerUser(RegisterUserRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserRegistrationException("User with this email already exists", null);
        }

        // Register user in Cognito
        SignUpResponse signUpResponse = cognitoService.signUp(request.getEmail(), request.getPassword(), request.getFullName());
        String cognitoSub = signUpResponse.userSub();

        // Create new user in DynamoDB
        UserEntity user = new UserEntity();
        user.setUserId(IdGenerator.userId());
        user.setUserType(UserType.CUSTOMER);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAuthProvider("EMAIL");
        user.setProviderUserId(request.getEmail());
        user.setCognitoUserId(cognitoSub);
        user.setStatus("ACTIVE");
        user.setCreatedAt(TimeUtil.now());
        user.setUpdatedAt(TimeUtil.now());
        userRepository.save(user);

        LOG.infof("New user registered: %s via EMAIL", user.getUserId());

        return new AuthResponse(user.getUserId(), true, toUserResponse(user));
    }

    public AuthResponse loginUser(LoginRequest request) {
        // Authenticate with Cognito
        InitiateAuthResponse authResult = cognitoService.signIn(request.getEmail(), request.getPassword());

        // Get user from DynamoDB
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new AuthResponse(user.getUserId(), false, toUserResponse(user),
                authResult.authenticationResult().accessToken(),
                authResult.authenticationResult().idToken());
    }

    public AuthResponse socialLogin(SocialAuthRequest request) {
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
        String email = safeGetClaim(claims, "email");
        if (email == null) email = request.getEmail();

        Optional<UserEntity> existing = userRepository.findByProvider(authProvider, providerUserId);
        boolean isNewUser = false;
        UserEntity user;

        if (existing.isPresent()) {
            user = existing.get();
            LOG.debugf("Existing user login: %s", user.getUserId());
            user.setUpdatedAt(TimeUtil.now());
            if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());
            if (request.getFullName() != null) user.setFullName(request.getFullName());
            userRepository.save(user);
        } else {
            isNewUser = true;
            user = new UserEntity();
            user.setUserId(IdGenerator.userId());
            user.setUserType(UserType.CUSTOMER);
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

    public UserResponse getCurrentUser() {
        AuthenticatedUser principal = requireAuthenticated();
        return userRepository.findByEmail(principal.getEmail())
                .map(this::toUserResponse)
                .orElseThrow(() -> new NotFoundException("User", principal.getCognitoUserId()));
    }

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
