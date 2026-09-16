package com.spring.securitytutorial.controller.model.response.admin;

import java.util.Set;
import java.util.UUID;

public record AdminUserApiResponse(UUID id, String username, boolean enabled,
                                   Set<String> roles, Set<String> permissions) {
}