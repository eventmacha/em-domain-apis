package com.eventmacha.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Set;

/**
 * JAX-RS filter that validates the Cognito JWT on every protected request.
 *
 * <p>Open (unauthenticated) paths:
 * <ul>
 *   <li>{@code POST /auth/social} – social login endpoint
 *   <li>{@code POST /webhooks/*} – Razorpay webhooks (verified by signature, not JWT)
 *   <li>{@code GET /plans} – public pricing page
 *   <li>{@code GET /openapi}, {@code GET /swagger-ui/*} – OpenAPI docs
 * </ul>
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtSecurityFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(JwtSecurityFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> OPEN_PATHS = Set.of(
            "/auth/social",
            "/plans"
    );
    private static final Set<String> OPEN_PREFIXES = Set.of(
            "/webhooks/",
            "/openapi",
            "/swagger-ui",
            "/q/"
    );

    @Inject
    CognitoJwtValidator jwtValidator;

    @Inject
    AuthenticatedUserContext userContext;

    // ── Request Filter ────────────────────────────────────────────────────────

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        if (isOpenPath(path)) {
            LOG.debugf("Skipping JWT validation for open path: %s", path);
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            LOG.debug("Missing or invalid Authorization header");
            requestContext.abortWith(unauthorizedResponse("Missing Bearer token"));
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            JWTClaimsSet claims = jwtValidator.validate(token);

            String cognitoSub = claims.getSubject();
            String email = claims.getStringClaim("email");
            String authProvider = jwtValidator.extractProvider(claims);

            AuthenticatedUser user = new AuthenticatedUser(cognitoSub, email, null, authProvider);
            userContext.set(user);

            LOG.debugf("Authenticated user: sub=%s email=%s provider=%s", cognitoSub, email, authProvider);

        } catch (com.eventmacha.exception.UnauthorizedException e) {
            LOG.debugf("JWT validation rejected: %s", e.getMessage());
            requestContext.abortWith(unauthorizedResponse(e.getMessage()));
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during JWT validation");
            requestContext.abortWith(unauthorizedResponse("Authentication failed"));
        }
    }

    // ── Response Filter (cleanup) ─────────────────────────────────────────────

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        userContext.clear();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isOpenPath(String path) {
        if (OPEN_PATHS.contains(path)) return true;
        return OPEN_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private jakarta.ws.rs.core.Response unauthorizedResponse(String message) {
        return jakarta.ws.rs.core.Response
                .status(401)
                .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE)
                .entity(new com.eventmacha.exception.ApiErrorResponse("UNAUTHORIZED", message))
                .build();
    }
}
