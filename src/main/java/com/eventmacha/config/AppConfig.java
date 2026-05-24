package com.eventmacha.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Typed application-level configuration (Cognito, Razorpay, SES).
 * All secrets must be injected via Lambda environment variables.
 */
@ConfigMapping(prefix = "event-macha")
public interface AppConfig {

    Cognito cognito();

    Razorpay razorpay();

    Ses ses();

    interface Cognito {
        String region();

        @WithName("user-pool-id")
        String userPoolId();
    }

    interface Razorpay {
        @WithName("key-id")
        String keyId();

        @WithName("key-secret")
        String keySecret();

        @WithName("webhook-secret")
        String webhookSecret();

        @WithName("base-url")
        String baseUrl();
    }

    interface Ses {
        String sender();

        String region();
    }
}
