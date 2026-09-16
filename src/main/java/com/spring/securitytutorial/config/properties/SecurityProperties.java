package com.spring.securitytutorial.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(
        @Valid @NotNull JwtSecurityProperties jwt,
        @Valid @NotNull RefreshTokenProperties refreshToken,
        @Valid @NotNull AuthSessionProperties session,
        @Valid @NotNull CorsSecurityProperties cors,
        @Valid @NotNull LoginThrottleProperties loginThrottle,
        @Valid @NotNull RetentionProperties retention,
        @Valid @NotNull WebhookSignatureProperties webhookSignature,
        @Valid @NotNull RequestBodyProperties requestBody,
        @Valid @NotNull RateLimitProperties rateLimit,
        @Valid @NotNull AccessTokenBlacklistProperties accessTokenBlacklist,
        @Valid BootstrapAdminProperties bootstrapAdmin,
        boolean seedDataEnabled) {
}
