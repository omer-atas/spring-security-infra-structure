package com.spring.securitytutorial.util.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationChannelValidationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        int channels = hasBearer(request) ? 1 : 0;
        channels += hasPair(request, ApiClientAuthenticationFilter.TENANT_KEY,
                ApiClientAuthenticationFilter.TENANT_SECRET) ? 1 : 0;
        channels += hasPair(request, ApiClientAuthenticationFilter.WEBHOOK_KEY,
                ApiClientAuthenticationFilter.WEBHOOK_SECRET) ? 1 : 0;
        if (channels > 1) {
            response.sendError(400, "Use exactly one authentication channel per request");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean hasBearer(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        return value != null && value.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    private boolean hasPair(HttpServletRequest request, String key, String secret) {
        return request.getHeader(key) != null || request.getHeader(secret) != null;
    }
}
