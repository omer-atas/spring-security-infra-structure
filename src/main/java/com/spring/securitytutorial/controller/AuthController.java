package com.spring.securitytutorial.controller;

import com.spring.securitytutorial.controller.model.request.auth.LoginApiRequest;
import com.spring.securitytutorial.controller.model.request.auth.RefreshApiRequest;
import com.spring.securitytutorial.controller.model.response.auth.CurrentUserApiResponse;
import com.spring.securitytutorial.controller.model.response.auth.SessionIdleApiResponse;
import com.spring.securitytutorial.controller.model.response.auth.TokenApiResponse;
import com.spring.securitytutorial.mapper.auth.AuthApiMapper;
import com.spring.securitytutorial.service.auth.AuthService;
import com.spring.securitytutorial.service.auth.SessionService;
import com.spring.securitytutorial.service.model.auth.AuthTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SessionService sessions;
    private final AuthApiMapper mapper;

    @PostMapping("/login")
    public ResponseEntity<TokenApiResponse> login(@Valid @RequestBody LoginApiRequest request, HttpServletRequest servletRequest) {
        String userAgent = servletRequest.getHeader("User-Agent");
        AuthTokenResponse authTokenResponse = authService.login(mapper.toRequest(request), servletRequest.getRemoteAddr(),
                userAgent);
        TokenApiResponse response = mapper.toResponse(authTokenResponse);
        return noStore(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenApiResponse> refresh(@Valid @RequestBody RefreshApiRequest refreshApiRequest) {
        AuthTokenResponse authTokenResponse = authService.refresh(mapper.toRequest(refreshApiRequest));
        TokenApiResponse response = mapper.toResponse(authTokenResponse);
        return noStore(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(JwtAuthenticationToken authentication) {
        String jti = authentication.getToken().getId();
        authService.logout(sessionId(authentication), authentication.getName(), jti);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public CurrentUserApiResponse me(Authentication authentication) {
        return mapper.toResponse(authService.currentUser(authentication));
    }

    @GetMapping("/session/idle")
    public SessionIdleApiResponse idle(JwtAuthenticationToken authentication) {
        return mapper.toResponse(sessions.idle(sessionId(authentication),
                UUID.fromString(Objects.requireNonNull(authentication.getToken().getSubject()))));
    }

    private UUID sessionId(JwtAuthenticationToken authentication) {
        return UUID.fromString(Objects.requireNonNull(authentication.getToken().getClaimAsString("sid")));
    }

    private ResponseEntity<TokenApiResponse> noStore(TokenApiResponse tokenApiResponse) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(tokenApiResponse);
    }
}
