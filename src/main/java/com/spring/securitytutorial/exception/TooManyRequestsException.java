package com.spring.securitytutorial.exception;

import java.time.Duration;

public class TooManyRequestsException extends RuntimeException {
    private final Duration retryAfter;

    public TooManyRequestsException(Duration retryAfter) {
        super("Too many login attempts. Retry in " + retryAfter.toSeconds() + " seconds.");
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}