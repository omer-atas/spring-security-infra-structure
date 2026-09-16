package com.spring.securitytutorial.service.model.order;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(UUID id, UUID customerId, String description, String status,
                            String createdBy, Instant createdAt) {
}