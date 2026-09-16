package com.spring.securitytutorial.service.model.token;

import java.time.Instant;

public record SessionIdleResponse(Instant idleExpiresAt, long warningBeforeSeconds) {
}