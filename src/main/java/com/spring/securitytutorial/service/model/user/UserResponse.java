package com.spring.securitytutorial.service.model.user;

import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id, String username, boolean enabled, Set<String> roles, Set<String> permissions) {
}