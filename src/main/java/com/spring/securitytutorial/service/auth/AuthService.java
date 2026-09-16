package com.spring.securitytutorial.service.auth;

import com.spring.securitytutorial.exception.InvalidRefreshTokenException;
import com.spring.securitytutorial.exception.TooManyRequestsException;
import com.spring.securitytutorial.service.model.auth.AuthSession;
import com.spring.securitytutorial.service.model.auth.AuthTokenResponse;
import com.spring.securitytutorial.service.model.auth.CurrentUserResponse;
import com.spring.securitytutorial.service.model.auth.LoginRequest;
import com.spring.securitytutorial.service.model.auth.RefreshTokenRequest;
import com.spring.securitytutorial.service.model.token.IssuedAccessToken;
import com.spring.securitytutorial.service.model.token.IssuedAccessTokenMetadata;
import com.spring.securitytutorial.service.model.token.IssuedRefreshToken;
import com.spring.securitytutorial.service.model.user.UserAccount;
import com.spring.securitytutorial.service.security.AccessTokenBlacklistService;
import com.spring.securitytutorial.service.security.LoginThrottleService;
import com.spring.securitytutorial.service.security.SecurityAuditService;
import com.spring.securitytutorial.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService users;
    private final SessionService sessions;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;
    private final SecurityAuditService audit;
    private final LoginThrottleService throttle;
    private final AccessTokenBlacklistService blacklist;
    private final Clock clock;

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String ip, String userAgent) {
        String username = request.username();
        Optional<Duration> blocked = throttle.retryAfter(ip, username);
        if (blocked.isPresent()) {
            throw new TooManyRequestsException(blocked.get());
        }
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (AuthenticationException exception) {
            audit.record("LOGIN_FAILED", username, null, exception.getClass().getSimpleName());
            throttle.registerFailure(ip, username);
            throw exception;
        }
        throttle.clear(ip, username);
        UserAccount user = users.findByUsername(request.username()).orElseThrow();
        AuthSession session = sessions.create(user, ip, userAgent);
        IssuedAccessToken access = accessTokens.issue(user, session);
        sessions.recordAccessToken(session.id(),
                new IssuedAccessTokenMetadata(access.jti(), access.expiresAt()));
        IssuedRefreshToken refresh = refreshTokens.issue(session.id());
        users.recordSuccessfulLogin(user.id(), clock.instant());
        audit.record("LOGIN_SUCCESS", user.username(), session.id(), "Password authentication");
        return pair(session.id(), access, refresh);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        var rotation = refreshTokens.rotate(request.refreshToken());
        AuthSession session = sessions.requireActive(rotation.sessionId());
        UserAccount user = users.findById(session.userId()).orElseThrow();
        IssuedAccessToken access = accessTokens.issue(user, session);
        sessions.recordAccessToken(session.id(),
                new IssuedAccessTokenMetadata(access.jti(), access.expiresAt()));
        audit.record("TOKEN_REFRESH", user.username(), session.id(), "Refresh token rotated");
        return pair(session.id(), access, rotation.refreshToken());
    }

    @Transactional
    public void logout(UUID sessionId, String principal, String currentAccessTokenJti) {
        sessions.logout(sessionId);
        refreshTokens.revokeSession(sessionId);
        if (currentAccessTokenJti != null) {
            blacklist.blacklist(currentAccessTokenJti, Duration.ofMinutes(15));
        }
        audit.record("LOGOUT", principal, sessionId, "User logout");
    }

    public CurrentUserResponse currentUser(Authentication authentication) {
        UUID sessionId = authentication instanceof JwtAuthenticationToken jwt
                ? UUID.fromString(jwt.getToken().getClaimAsString("sid")) : null;
        return new CurrentUserResponse(authentication.getName(), authentication.getClass().getSimpleName(),
                authentication.getAuthorities().stream().map(Object::toString).sorted().toList(), sessionId);
    }

    private AuthTokenResponse pair(UUID sessionId, IssuedAccessToken access, IssuedRefreshToken refresh) {
        var now = clock.instant();
        return new AuthTokenResponse(access.value(), refresh.value(), "Bearer",
                Math.max(0, Duration.between(now, access.expiresAt()).toSeconds()),
                Math.max(0, Duration.between(now, refresh.expiresAt()).toSeconds()),
                access.expiresAt(), refresh.expiresAt(), sessionId);
    }
}
