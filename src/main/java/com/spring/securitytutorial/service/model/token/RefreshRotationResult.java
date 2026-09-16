package com.spring.securitytutorial.service.model.token;

import java.util.UUID;

public record RefreshRotationResult(UUID sessionId, IssuedRefreshToken refreshToken) {
}