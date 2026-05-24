package com.eventmacha.security;

import com.eventmacha.config.AppConfig;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.net.URL;
import java.util.Map;

/**
 * Validates Cognito-issued JWTs using Nimbus JOSE+JWT.
 *
 * <p>The JWKS endpoint is fetched once and cached by RemoteJWKSet.
 * This is optimised for Lambda cold starts – no eager HTTP call at startup,
 * the key set is fetched on first token validation and then cached.
 */
@ApplicationScoped
public class CognitoJwtValidator {

    private static final Logger LOG = Logger.getLogger(CognitoJwtValidator.class);

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String expectedIssuer;

    @Inject
    public CognitoJwtValidator(AppConfig appConfig) {
        String region = appConfig.cognito().region();
        String userPoolId = appConfig.cognito().userPoolId();
        this.expectedIssuer = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;

        try {
            URL jwksUrl = new URL(expectedIssuer + "/.well-known/jwks.json");
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(jwksUrl);
            var keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(keySelector);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure Cognito JWT validator", e);
        }
    }

    /**
     * Validate and parse a Cognito JWT.
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return parsed claims
     * @throws com.eventmacha.exception.UnauthorizedException if invalid
     */
    public JWTClaimsSet validate(String token) {
        try {
            JWTClaimsSet claims = jwtProcessor.process(token, null);

            // Validate issuer
            String issuer = claims.getIssuer();
            if (!expectedIssuer.equals(issuer)) {
                LOG.warnf("JWT issuer mismatch: expected=%s actual=%s", expectedIssuer, issuer);
                throw new com.eventmacha.exception.UnauthorizedException("Invalid token issuer");
            }

            // Validate token_use (must be 'access' or 'id')
            Object tokenUse = claims.getClaim("token_use");
            if (tokenUse == null || (!tokenUse.toString().equals("id") && !tokenUse.toString().equals("access"))) {
                throw new com.eventmacha.exception.UnauthorizedException("Invalid token_use claim");
            }

            return claims;
        } catch (com.eventmacha.exception.UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            LOG.debugf(e, "JWT validation failed");
            throw new com.eventmacha.exception.UnauthorizedException("Invalid or expired token", e);
        }
    }

    /**
     * Extract the auth provider from token claims.
     * Cognito sets "identities" for federated users; falls back to "cognito" for email/password.
     */
    public String extractProvider(JWTClaimsSet claims) {
        try {
            var identities = claims.getListClaim("identities");
            if (identities != null && !identities.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> identity = (Map<String, Object>) identities.get(0);
                String providerName = (String) identity.get("providerName");
                if (providerName != null) {
                    return providerName.toUpperCase();
                }
            }
        } catch (Exception e) {
            LOG.debugf("Could not extract identity provider from token");
        }
        return "EMAIL";
    }

    /**
     * Extract the provider subject (providerUserId) from token claims.
     */
    public String extractProviderUserId(JWTClaimsSet claims) {
        try {
            var identities = claims.getListClaim("identities");
            if (identities != null && !identities.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> identity = (Map<String, Object>) identities.get(0);
                String userId = (String) identity.get("userId");
                if (userId != null) return userId;
            }
        } catch (Exception e) {
            LOG.debugf("Could not extract providerUserId from token");
        }
        // Fall back to Cognito sub
        return claims.getSubject();
    }
}
