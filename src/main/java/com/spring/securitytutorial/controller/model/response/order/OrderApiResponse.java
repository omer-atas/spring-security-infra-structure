package com.spring.securitytutorial.controller.model.response.order;

import java.time.Instant;
import java.util.UUID;

public record OrderApiResponse(UUID id, UUID customerId, String description, String status,
                               String createdBy, Instant createdAt) {
}