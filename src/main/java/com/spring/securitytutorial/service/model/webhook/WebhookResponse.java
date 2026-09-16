package com.spring.securitytutorial.service.model.webhook;

import java.time.Instant;

public record WebhookResponse(String eventType, String acceptedBy, Instant acceptedAt) {
}