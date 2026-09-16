package com.spring.securitytutorial.util.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spring.securitytutorial.config.properties.RateLimitProperties;
import com.spring.securitytutorial.config.properties.RateLimitProperties.EndpointRateLimit;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties rateLimitProperties;
    private final Cache<String, Bucket> localBuckets;
    private final Environment environment;

    public RateLimitFilter(RateLimitProperties rateLimitProperties, Environment environment) {
        this.rateLimitProperties = rateLimitProperties;
        this.environment = environment;
        this.localBuckets = Caffeine.newBuilder()
                .maximumSize(rateLimitProperties.cacheMaximumSize())
                .expireAfterAccess(rateLimitProperties.cacheExpireAfterAccess())
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!environment.getProperty("app.security.rate-limit-filter.enabled", Boolean.class, true)) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        EndpointRateLimit limit = rateLimitProperties.resolve(path);
        String key = "rate-limit:" + clientIp(request) + ":" + normalizePath(path);

        Bucket bucket = getLocalBucket(key, limit);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            respondTooManyRequests(response, probe.getNanosToWaitForRefill());
            return;
        }
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit.capacity()));
        filterChain.doFilter(request, response);
    }

    private Bucket getLocalBucket(String key, EndpointRateLimit limit) {
        return localBuckets.get(key, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(limit.capacity(),
                        Refill.greedy(limit.refillTokens(), limit.refillPeriod())))
                .build());
    }

    private String normalizePath(String path) {
        if (path.matches("/api/v1/orders/[0-9a-fA-F\\-]{36}")) {
            return "/api/v1/orders/{id}";
        }
        if (path.matches("/api/admin/sessions/[0-9a-fA-F\\-]{36}/revoke")) {
            return "/api/admin/sessions/{id}/revoke";
        }
        if (path.matches("/api/users/[0-9a-fA-F\\-]{36}")) {
            return "/api/users/{id}";
        }
        return path;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void respondTooManyRequests(HttpServletResponse response, long nanosToWait) throws IOException {
        long retryAfterSeconds = Math.max(1, (nanosToWait / 1_000_000_000L) + 1);
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{"
                + "\"title\":\"Too Many Requests\",\"status\":429,"
                + "\"detail\":\"Rate limit exceeded. Retry after " + retryAfterSeconds + " seconds.\""
                + "}");
    }
}
