package com.eventmacha.service;

import com.eventmacha.exception.UnauthorizedException;
import com.eventmacha.exception.UserRegistrationException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@ApplicationScoped
public class CognitoService {

    @ConfigProperty(name = "aws.cognito.clientId")
    String clientId;

    private final CognitoIdentityProviderClient cognitoClient;

    public CognitoService() {
        this.cognitoClient = CognitoIdentityProviderClient.create();
    }

    public SignUpResponse signUp(String email, String password, String fullName) {
        try {
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .password(password)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("name").value(fullName).build()
                    )
                    .build();
            return cognitoClient.signUp(signUpRequest);
        } catch (Exception e) {
            throw new UserRegistrationException("Failed to register user in Cognito", e);
        }
    }

    public InitiateAuthResponse signIn(String email, String password) {
        try {
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(Map.of(
                            "USERNAME", email,
                            "PASSWORD", password
                    ))
                    .build();
            return cognitoClient.initiateAuth(authRequest);
        } catch (NotAuthorizedException | UserNotFoundException e) {
            throw new UnauthorizedException("Invalid email or password");
        } catch (Exception e) {
            throw new UnauthorizedException("Login failed");
        }
    }
}