package com.spring.securitytutorial.service.model.auth;

import java.util.List;
import java.util.UUID;

public record CurrentUserResponse(
        String username,
        String authenticationType,
        List<String> authorities,
        UUID sessionId) {
}