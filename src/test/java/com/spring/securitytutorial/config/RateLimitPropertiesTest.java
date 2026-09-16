package com.spring.securitytutorial.config;

import com.spring.securitytutorial.config.properties.RateLimitProperties;
import com.spring.securitytutorial.config.properties.RateLimitProperties.EndpointRateLimit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RateLimitPropertiesTest {

    @Test
    void bindsGlobalAndEndpointSpecificLimits() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "app.security.rate-limit.capacity", "20",
                "app.security.rate-limit.refill-tokens", "10",
                "app.security.rate-limit.refill-period", "PT1M",
                "app.security.rate-limit.endpoints[/api/auth/login].capacity", "5",
                "app.security.rate-limit.endpoints[/api/auth/login].refill-tokens", "5",
                "app.security.rate-limit.endpoints[/api/auth/login].refill-period", "PT1M"));

        RateLimitProperties properties = new Binder(source)
                .bind("app.security.rate-limit", Bindable.of(RateLimitProperties.class))
                .get();

        assertEquals(20, properties.capacity());
        assertEquals(Duration.ofMinutes(1), properties.refillPeriod());

        EndpointRateLimit login = properties.resolve("/api/auth/login");
        assertNotNull(login);
        assertEquals(5, login.capacity());
        assertEquals(5, login.refillTokens());
    }

    @Test
    void fallsBackToGlobalForUnconfiguredPaths() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "app.security.rate-limit.capacity", "20",
                "app.security.rate-limit.refill-tokens", "10",
                "app.security.rate-limit.refill-period", "PT1M"));

        RateLimitProperties properties = new Binder(source)
                .bind("app.security.rate-limit", Bindable.of(RateLimitProperties.class))
                .get();

        EndpointRateLimit fallback = properties.resolve("/api/unknown");
        assertEquals(20, fallback.capacity());
        assertEquals(Duration.ofMinutes(1), fallback.refillPeriod());
    }

    @Test
    void endpointPrefixIsLongestMatch() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "app.security.rate-limit.capacity", "20",
                "app.security.rate-limit.refill-tokens", "10",
                "app.security.rate-limit.refill-period", "PT1M",
                "app.security.rate-limit.endpoints[/api/v1/orders].capacity", "30",
                "app.security.rate-limit.endpoints[/api/v1/orders].refill-tokens", "30",
                "app.security.rate-limit.endpoints[/api/v1/orders].refill-period", "PT1M",
                "app.security.rate-limit.endpoints[/api/v1/orders/{id}].capacity", "50",
                "app.security.rate-limit.endpoints[/api/v1/orders/{id}].refill-tokens", "50",
                "app.security.rate-limit.endpoints[/api/v1/orders/{id}].refill-period", "PT1M"));

        RateLimitProperties properties = new Binder(source)
                .bind("app.security.rate-limit", Bindable.of(RateLimitProperties.class))
                .get();

        assertEquals(30, properties.resolve("/api/v1/orders").capacity());
    }
}