package com.spring.securitytutorial.util.security;

import com.spring.securitytutorial.exception.InvalidSessionException;
import com.spring.securitytutorial.service.auth.SessionService;
import com.spring.securitytutorial.service.security.AccessTokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionValidationFilter extends OncePerRequestFilter {
    private final SessionService sessions;
    private final AccessTokenBlacklistService blacklist;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt && authentication.isAuthenticated()) {
            String jti = jwt.getToken().getId();
            if (jti != null && blacklist.isBlacklisted(jti)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), Map.of(
                        "title", "Token revoked", "status", 401, "detail", "Access token has been revoked"));
                return;
            }
            try {
                sessions.validateAndTouch(UUID.fromString(Objects.requireNonNull(jwt.getToken().getClaimAsString("sid"))),
                        UUID.fromString(Objects.requireNonNull(jwt.getToken().getSubject())));
            } catch (InvalidSessionException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), Map.of(
                        "title", "Invalid session", "status", 401, "detail", exception.getMessage()));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
