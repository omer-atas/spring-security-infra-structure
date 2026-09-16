package com.spring.securitytutorial.service.model.token;

import java.time.Instant;
import java.util.UUID;

public record IssuedAccessTokenMetadata(UUID jti, Instant expiresAt) {
}