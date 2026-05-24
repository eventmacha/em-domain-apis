package com.eventmacha.security;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * ThreadLocal holder for the current request's authenticated user.
 *
 * <p>Safe in AWS Lambda because each Lambda invocation runs in a single thread
 * (Quarkus processes one request at a time per container instance).
 * The filter clears the ThreadLocal after each request to prevent stale data
 * across warm reuse of the same Lambda container.
 */
@ApplicationScoped
public class AuthenticatedUserContext {

    private static final ThreadLocal<AuthenticatedUser> HOLDER = new ThreadLocal<>();

    /** Store the authenticated user for this request thread. */
    public void set(AuthenticatedUser user) {
        HOLDER.set(user);
    }

    /** Retrieve the authenticated user for this request. Returns {@code null} if not set. */
    public AuthenticatedUser get() {
        return HOLDER.get();
    }

    /** Remove the authenticated user after the request completes. */
    public void clear() {
        HOLDER.remove();
    }

    /** Returns {@code true} if there is an authenticated user on the current thread. */
    public boolean isAuthenticated() {
        return HOLDER.get() != null;
    }
}
