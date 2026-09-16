package com.spring.securitytutorial.controller.model.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginApiRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 200) String password) {
}