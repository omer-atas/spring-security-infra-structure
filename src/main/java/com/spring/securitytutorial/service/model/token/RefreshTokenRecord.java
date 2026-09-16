package com.spring.securitytutorial.service.model.token;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenRecord(
        UUID id,
        UUID sessionId,
        UUID familyId,
        String tokenHash,
        Instant createdAt,
        Instant expiresAt,
        Instant usedAt,
        Instant revokedAt,
        UUID replacedByTokenId) {
}
