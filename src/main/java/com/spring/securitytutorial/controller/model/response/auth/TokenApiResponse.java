package com.spring.securitytutorial.controller.model.response.auth;

import java.time.Instant;
import java.util.UUID;

public record TokenApiResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        UUID sessionId) {
}