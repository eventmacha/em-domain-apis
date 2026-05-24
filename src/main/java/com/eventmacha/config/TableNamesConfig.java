package com.eventmacha.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Typed configuration mapping for all DynamoDB table names.
 * Values are injected via Lambda environment variables, falling back to defaults.
 */
@ConfigMapping(prefix = "event-macha.tables")
public interface TableNamesConfig {

    @WithName("users")
    String users();

    @WithName("rate-cards")
    String rateCards();

    @WithName("rate-card-plans")
    String rateCardPlans();

    @WithName("orders")
    String orders();

    @WithName("payments")
    String payments();

    @WithName("payment-history")
    String paymentHistory();

    @WithName("publish")
    String publish();

    @WithName("publish-history")
    String publishHistory();
}
