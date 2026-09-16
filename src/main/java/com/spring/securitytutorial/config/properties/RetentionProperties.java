package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record RetentionProperties(@NotNull Duration session,
                                  @NotNull Duration refreshToken,
                                  @NotNull Duration audit) {
}