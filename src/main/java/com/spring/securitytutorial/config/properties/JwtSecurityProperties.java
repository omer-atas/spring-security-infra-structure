package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.security.jwt")
public record JwtSecurityProperties(@NotBlank String issuer, @NotBlank String audience,
                                    @NotNull Duration accessTokenTtl, String rsaPrivateKeyPem,
                                    String rsaPublicKeyPem) {
}
