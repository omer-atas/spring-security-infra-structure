package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.security.login-throttle")
public record LoginThrottleProperties(@Min(1) @Max(100) int maxAttempts,
                                      @NotNull Duration window) {
}