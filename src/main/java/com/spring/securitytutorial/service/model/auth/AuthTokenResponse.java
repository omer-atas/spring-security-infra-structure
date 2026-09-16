package com.spring.securitytutorial.service.model.auth;

import java.time.Instant;
import java.util.UUID;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        UUID sessionId) {
}