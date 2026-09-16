package com.spring.securitytutorial.controller.model.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderApiRequest(@NotBlank @Size(max = 500) String description) {
}