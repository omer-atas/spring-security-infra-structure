package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CorsSecurityProperties(@NotEmpty List<@NotBlank String> allowedOriginPatterns) {
}
