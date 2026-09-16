package com.spring.securitytutorial.service.model.auth;

import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.service.model.enums.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public record AuthSession(
        UUID id,
        UUID userId,
        SessionStatus status,
        RevokeReason revokeReason,
        Instant loginAt,
        Instant expiresAt,
        Instant lastActivityAt,
        Instant logoutAt,
        String ipAddress,
        String userAgent,
        UUID currentAccessTokenJti,
        Instant accessTokenExpiresAt,
        long version) {
}
