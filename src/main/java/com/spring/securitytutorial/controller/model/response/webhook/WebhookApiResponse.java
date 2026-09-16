package com.spring.securitytutorial.controller.model.response.webhook;

import java.time.Instant;

public record WebhookApiResponse(String eventType, String acceptedBy, Instant acceptedAt) {
}