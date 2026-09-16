package com.spring.securitytutorial.controller.model.response.auth;

import java.time.Instant;

public record SessionIdleApiResponse(Instant idleExpiresAt, long warningBeforeSeconds) {
}