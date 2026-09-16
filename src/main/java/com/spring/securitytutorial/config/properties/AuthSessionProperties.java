package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record AuthSessionProperties(@NotNull Duration absoluteTimeout, @NotNull Duration idleTimeout,
                                    @NotNull Duration activityUpdateInterval, @NotNull Duration warningBefore) {
}
