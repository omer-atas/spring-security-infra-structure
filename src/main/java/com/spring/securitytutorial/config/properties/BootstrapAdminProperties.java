package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotBlank;

public record BootstrapAdminProperties(@NotBlank String username, @NotBlank String password) {
}