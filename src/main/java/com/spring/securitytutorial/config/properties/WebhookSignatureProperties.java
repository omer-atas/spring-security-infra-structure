package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.security.webhook-signature")
public record WebhookSignatureProperties(@NotNull Duration ttl) {
}