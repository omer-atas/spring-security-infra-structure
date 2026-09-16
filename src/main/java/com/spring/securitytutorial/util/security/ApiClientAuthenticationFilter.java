package com.spring.securitytutorial.util.security;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.service.model.enums.ApiClientType;
import com.spring.securitytutorial.util.constant.RestApiHeaderNames;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiClientAuthenticationFilter extends OncePerRequestFilter {
    public static final String TENANT_KEY = RestApiHeaderNames.TENANT_KEY;
    public static final String TENANT_SECRET = RestApiHeaderNames.TENANT_SECRET;
    public static final String WEBHOOK_KEY = RestApiHeaderNames.WEBHOOK_KEY;
    public static final String WEBHOOK_SECRET = RestApiHeaderNames.WEBHOOK_SECRET;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final WebhookSignatureVerifier signatureVerifier;
    private final SecurityProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        ApiClientType type = typeFor(request.getRequestURI());
        if (type == null) {
            chain.doFilter(request, response);
            return;
        }
        String key = request.getHeader(type == ApiClientType.TENANT ? TENANT_KEY : WEBHOOK_KEY);
        String secret = request.getHeader(type == ApiClientType.TENANT ? TENANT_SECRET : WEBHOOK_SECRET);
        if (key == null || secret == null) {
            chain.doFilter(request, response);
            return;
        }
        if (hasContentLengthExceeding(request)) {
            respondPayloadTooLarge(response);
            return;
        }
        UUID currentUserId = parseCurrentUserId(request, response);
        if (response.getStatus() >= 400) {
            return;
        }
        HttpServletRequest processed = request;
        if (type == ApiClientType.WEBHOOK) {
            processed = new CachedBodyHttpServletRequest(request, maxBodyBytes());
        }
        try {
            Authentication authenticated = authenticationManager.authenticate(
                    ApiClientAuthenticationToken.unauthenticated(key, secret, type, currentUserId));
            if (type == ApiClientType.WEBHOOK) {
                ApiClientAuthenticationToken token = (ApiClientAuthenticationToken) authenticated;
                verifyWebhookSignature(token.signingSecret(),
                        request.getHeader(RestApiHeaderNames.WEBHOOK_TIMESTAMP),
                        request.getHeader(RestApiHeaderNames.WEBHOOK_SIGNATURE),
                        ((CachedBodyHttpServletRequest) processed).getCachedBody());
            }
            SecurityContextHolder.getContext().setAuthentication(authenticated);
            chain.doFilter(processed, response);
        } catch (CachedBodyHttpServletRequest.RequestSizeLimitExceededException exception) {
            SecurityContextHolder.clearContext();
            respondPayloadTooLarge(response);
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            respondUnauthorized(response, exception);
        }
    }

    private UUID parseCurrentUserId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String currentUser = request.getHeader(RestApiHeaderNames.CURRENT_USER_ID);
        if (currentUser == null || currentUser.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(currentUser);
        } catch (IllegalArgumentException exception) {
            response.setStatus(400);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "title", "Bad Request", "status", 400,
                    "detail", "X-Current-User-Id must be a valid UUID"));
            return null;
        }
    }

    private boolean hasContentLengthExceeding(HttpServletRequest request) {
        long length = request.getContentLengthLong();
        return length >= 0 && length > maxBodyBytes();
    }

    private long maxBodyBytes() {
        return properties.requestBody().maxSize().toBytes();
    }

    private void verifyWebhookSignature(String secret, String timestamp, String signature, byte[] body) {
        if (!signatureVerifier.verify(secret, timestamp, signature, body)) {
            throw new BadCredentialsException("Webhook signature is invalid or expired");
        }
    }

    private void respondUnauthorized(HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "title", "Authentication failed", "status", 401, "detail", exception.getMessage()));
    }

    private void respondPayloadTooLarge(HttpServletResponse response) throws IOException {
        response.setStatus(413);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "title", "Payload Too Large", "status", 413, "detail", "Request body exceeds the allowed size"));
    }

    private ApiClientType typeFor(String path) {
        if (path.startsWith("/api/v1/orders")) return ApiClientType.TENANT;
        if (path.startsWith("/api/webhooks")) return ApiClientType.WEBHOOK;
        return null;
    }
}