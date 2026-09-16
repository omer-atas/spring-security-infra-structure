package com.spring.securitytutorial.service.model.user;

import java.util.Set;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String username,
        String passwordHash,
        boolean enabled,
        Set<String> roles,
        Set<String> permissions) {
}
