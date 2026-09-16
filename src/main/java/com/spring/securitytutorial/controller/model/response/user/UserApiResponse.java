package com.spring.securitytutorial.controller.model.response.user;

import java.util.Set;
import java.util.UUID;

public record UserApiResponse(UUID id, String username, boolean enabled, Set<String> roles, Set<String> permissions) {
}