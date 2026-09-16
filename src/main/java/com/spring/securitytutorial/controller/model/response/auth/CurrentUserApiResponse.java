package com.spring.securitytutorial.controller.model.response.auth;

import java.util.List;
import java.util.UUID;

public record CurrentUserApiResponse(
        String username,
        String authenticationType,
        List<String> authorities,
        UUID sessionId) {
}