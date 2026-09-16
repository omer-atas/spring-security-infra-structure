package com.spring.securitytutorial.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties("app.security.rate-limit")
public record RateLimitProperties(@Min(1) long capacity,
                                  @Min(1) long refillTokens,
                                  @NotNull Duration refillPeriod,
                                  Map<String, EndpointRateLimit> endpoints,
                                  @Min(100) long cacheMaximumSize,
                                  @NotNull Duration cacheExpireAfterAccess) {

    public record EndpointRateLimit(@Min(1) long capacity,
                                    @Min(1) long refillTokens,
                                    @NotNull Duration refillPeriod) {
    }

    public EndpointRateLimit resolve(String path) {
        EndpointRateLimit best = null;
        int bestLength = -1;
        if (endpoints != null) {
            for (var entry : endpoints.entrySet()) {
                if (path.startsWith(entry.getKey()) && entry.getKey().length() > bestLength) {
                    best = entry.getValue();
                    bestLength = entry.getKey().length();
                }
            }
        }
        return best != null ? best : new EndpointRateLimit(capacity, refillTokens, refillPeriod);
    }
}
