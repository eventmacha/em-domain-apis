package com.eventmacha.util;

import java.time.Instant;

/**
 * Epoch-millisecond time utilities used throughout the domain layer.
 * DynamoDB stores all timestamps as Number (Long / epoch ms).
 */
public final class TimeUtil {

    private TimeUtil() {}

    /** Current epoch milliseconds. */
    public static long now() {
        return Instant.now().toEpochMilli();
    }

    /** Current epoch seconds. */
    public static long nowSeconds() {
        return Instant.now().getEpochSecond();
    }

    /** Future epoch ms, {@code daysFromNow} days ahead. */
    public static long daysFromNow(int daysFromNow) {
        return Instant.now().plusSeconds((long) daysFromNow * 86_400).toEpochMilli();
    }

    /** Check whether a given epoch-ms timestamp has passed. */
    public static boolean isExpired(Long epochMs) {
        if (epochMs == null) return false;
        return Instant.now().toEpochMilli() > epochMs;
    }
}
