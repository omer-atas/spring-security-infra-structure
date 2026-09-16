package com.spring.securitytutorial.service.model.token;

import java.time.Instant;
import java.util.UUID;

public record IssuedAccessToken(String value, UUID jti, Instant expiresAt) {
}
