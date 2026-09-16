package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("app.security.request-body")
public record RequestBodyProperties(@NotNull DataSize maxSize) {
}