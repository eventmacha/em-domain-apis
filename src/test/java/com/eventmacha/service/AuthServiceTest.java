package com.eventmacha.service;

import com.eventmacha.dto.request.SocialAuthRequest;
import com.eventmacha.dto.response.AuthResponse;
import com.eventmacha.entity.UserEntity;
import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.repository.UserRepository;
import com.eventmacha.security.AuthenticatedUser;
import com.eventmacha.security.AuthenticatedUserContext;
import com.eventmacha.security.CognitoJwtValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    CognitoJwtValidator jwtValidator;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthenticatedUserContext userContext;

    @InjectMocks
    AuthService authService;

    private JWTClaimsSet mockClaims;
    private SocialAuthRequest request;

    @BeforeEach
    void setUp() throws Exception {
        mockClaims = new JWTClaimsSet.Builder()
                .subject("cognito-sub-123")
                .claim("email", "test@example.com")
                .build();

        request = new SocialAuthRequest();
        request.setProvider("GOOGLE");
        request.setIdToken("valid.jwt.token");
        request.setEmail("test@example.com");
        request.setFullName("Test User");
    }

    // ── socialLogin ───────────────────────────────────────────────────────────

    @Test
    void socialLogin_newUser_createsAndReturnsUser() {
        when(jwtValidator.validate(anyString())).thenReturn(mockClaims);
        when(jwtValidator.extractProvider(any())).thenReturn("GOOGLE");
        when(jwtValidator.extractProviderUserId(any())).thenReturn("google-uid-123");
        when(userRepository.findByProvider("GOOGLE", "google-uid-123")).thenReturn(Optional.empty());

        AuthResponse response = authService.socialLogin(request);

        assertTrue(response.isNewUser());
        assertNotNull(response.getUserId());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void socialLogin_existingUser_updatesAndReturnsUser() {
        UserEntity existing = new UserEntity();
        existing.setUserId("existing-user-id");
        existing.setEmail("test@example.com");
        existing.setAuthProvider("GOOGLE");
        existing.setProviderUserId("google-uid-123");
        existing.setStatus("ACTIVE");
        existing.setCreatedAt(System.currentTimeMillis());
        existing.setUpdatedAt(System.currentTimeMillis());

        when(jwtValidator.validate(anyString())).thenReturn(mockClaims);
        when(jwtValidator.extractProvider(any())).thenReturn("GOOGLE");
        when(jwtValidator.extractProviderUserId(any())).thenReturn("google-uid-123");
        when(userRepository.findByProvider("GOOGLE", "google-uid-123")).thenReturn(Optional.of(existing));

        AuthResponse response = authService.socialLogin(request);

        assertFalse(response.isNewUser());
        assertEquals("existing-user-id", response.getUserId());
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void socialLogin_invalidToken_throwsUnauthorized() {
        when(jwtValidator.validate(anyString()))
                .thenThrow(new UnauthorizedException("Invalid token"));

        assertThrows(UnauthorizedException.class, () -> authService.socialLogin(request));
        verify(userRepository, never()).save(any());
    }

    // ── getCurrentUser ────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_authenticated_returnsProfile() {
        AuthenticatedUser principal = new AuthenticatedUser("sub-123", "test@example.com", "uid-1", "GOOGLE");
        when(userContext.get()).thenReturn(principal);

        UserEntity entity = new UserEntity();
        entity.setUserId("uid-1");
        entity.setEmail("test@example.com");
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(entity));

        var result = authService.getCurrentUser();
        assertEquals("uid-1", result.getUserId());
    }

    @Test
    void getCurrentUser_notAuthenticated_throwsUnauthorized() {
        when(userContext.get()).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser());
    }
}
