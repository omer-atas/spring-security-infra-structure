package com.spring.securitytutorial.service.security;

import com.spring.securitytutorial.util.security.ApiClientAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentCustomerProvider {
    public UUID getRequiredCustomerId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Current customer was not found in SecurityContext"));
    }

    public Optional<UUID> getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ApiClientAuthenticationToken tenant && tenant.currentUserId() != null) {
            return Optional.of(tenant.currentUserId());
        }
        if (authentication instanceof JwtAuthenticationToken jwt) {
            return Optional.of(UUID.fromString(Objects.requireNonNull(jwt.getToken().getSubject())));
        }
        return Optional.empty();
    }
}