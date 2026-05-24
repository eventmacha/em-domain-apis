package com.eventmacha.util;

import java.util.UUID;

/**
 * Generates collision-resistant identifiers for all domain entities.
 */
public final class IdGenerator {

    private IdGenerator() {}

    /** Random UUID without hyphens. */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** Prefixed ID, e.g. "ord_abc123..." */
    public static String generate(String prefix) {
        return prefix + "_" + generate();
    }

    public static String userId() {
        return generate("usr");
    }

    public static String orderId() {
        return generate("ord");
    }

    public static String paymentId() {
        return generate("pay");
    }

    public static String rateCardId() {
        return generate("rc");
    }

    public static String publishVersionId() {
        return generate("pv");
    }
}
