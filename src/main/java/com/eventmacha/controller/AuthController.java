package com.eventmacha.controller;

import com.eventmacha.common.ApiResponse;
import com.eventmacha.dto.request.LoginRequest;
import com.eventmacha.dto.request.RegisterUserRequest;
import com.eventmacha.dto.request.SocialAuthRequest;
import com.eventmacha.dto.response.AuthResponse;
import com.eventmacha.dto.response.UserResponse;
import com.eventmacha.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Authentication endpoints – social login (Google/Apple/Email) and profile retrieval.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Cognito-based social and email authentication")
public class AuthController {

    private static final Logger LOG = Logger.getLogger(AuthController.class);

    @Inject
    AuthService authService;

    /**
     * POST /auth/register
     * Registers a new user in Cognito and DynamoDB.
     * This endpoint is open (no Bearer token required).
     */
    @POST
    @Path("/register")
    @Operation(summary = "Register New User",
            description = "Registers a new user with email and password, creating entries in Cognito and DynamoDB.")
    @APIResponse(responseCode = "200", description = "Registration successful")
    @APIResponse(responseCode = "400", description = "Validation error or user already exists")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RegisterUserRequest.class)))
    public Response register(@Valid RegisterUserRequest request) {
        LOG.debugf("POST /auth/register email=%s", request.getEmail());
        AuthResponse authResponse = authService.registerUser(request);
        return Response.ok(ApiResponse.ok(authResponse)).build();
    }

    /**
     * POST /auth/login
     * Authenticate user using email and password against Cognito.
     * Returns JWT tokens upon successful authentication.
     */
    @POST
    @Path("/login")
    @Operation(summary = "Login with Email and Password",
            description = "Authenticates the user with Cognito using email and password and returns JWT tokens.")
    @APIResponse(responseCode = "200", description = "Login successful")
    @APIResponse(responseCode = "400", description = "Validation error")
    @APIResponse(responseCode = "401", description = "Invalid credentials")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoginRequest.class)))
    public Response login(@Valid LoginRequest request) {
        LOG.debugf("POST /auth/login email=%s", request.getEmail());
        AuthResponse authResponse = authService.loginUser(request);
        return Response.ok(ApiResponse.ok(authResponse)).build();
    }

    /**
     * POST /auth/social
     * Validates a Cognito JWT, upserts the user profile, and returns user info.
     * This endpoint is open (no Bearer token required).
     */
    @POST
    @Path("/social")
    @Operation(summary = "Social / Email Login",
            description = "Validates a Cognito idToken, upserts the user profile in DynamoDB, " +
                    "and returns user info. Supports GOOGLE, APPLE, and EMAIL providers.")
    @APIResponse(responseCode = "200", description = "Login successful")
    @APIResponse(responseCode = "400", description = "Validation error")
    @APIResponse(responseCode = "401", description = "Invalid token")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SocialAuthRequest.class)))
    public Response socialLogin(@Valid SocialAuthRequest request) {
        LOG.debugf("POST /auth/social provider=%s", request.getProvider());
        AuthResponse authResponse = authService.socialLogin(request);
        return Response.ok(ApiResponse.ok(authResponse)).build();
    }

    /**
     * GET /auth/me
     * Returns the profile of the currently authenticated user.
     * Requires a valid Bearer token.
     */
    @GET
    @Path("/me")
    @Operation(summary = "Get Current User Profile",
            description = "Returns the DynamoDB user profile for the authenticated user.")
    @APIResponse(responseCode = "200", description = "User profile")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response getCurrentUser() {
        LOG.debug("GET /auth/me");
        UserResponse user = authService.getCurrentUser();
        return Response.ok(ApiResponse.ok(user)).build();
    }
}
