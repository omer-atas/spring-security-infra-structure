package com.spring.securitytutorial.controller.model.request.webhook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookApiRequest(@NotBlank @Size(max = 200) String eventType) {
}