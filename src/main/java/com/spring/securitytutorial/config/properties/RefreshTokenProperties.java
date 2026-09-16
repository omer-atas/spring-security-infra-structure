package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record RefreshTokenProperties(@NotNull Duration ttl) {
}
