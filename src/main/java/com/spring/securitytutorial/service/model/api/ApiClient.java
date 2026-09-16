package com.spring.securitytutorial.service.model.api;

import com.spring.securitytutorial.service.model.enums.ApiClientType;

import java.util.Set;
import java.util.UUID;

public record ApiClient(
        UUID id,
        String name,
        ApiClientType type,
        String key,
        String secretHash,
        String signingSecret,
        boolean enabled,
        boolean currentUserRequired,
        Set<String> authorities) {
}
