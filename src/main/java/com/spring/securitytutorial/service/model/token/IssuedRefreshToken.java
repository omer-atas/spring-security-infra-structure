package com.spring.securitytutorial.service.model.token;

import java.time.Instant;
import java.util.UUID;

public record IssuedRefreshToken(String value, UUID id, UUID familyId, Instant expiresAt) {
}
