package com.spring.securitytutorial.controller.model.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshApiRequest(@NotBlank @Size(max = 512) String refreshToken) {
}