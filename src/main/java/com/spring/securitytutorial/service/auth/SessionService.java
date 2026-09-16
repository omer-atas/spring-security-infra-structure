package com.spring.securitytutorial.service.auth;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.entity.SessionEntity;
import com.spring.securitytutorial.exception.InvalidSessionException;
import com.spring.securitytutorial.repository.SessionJpaRepository;
import com.spring.securitytutorial.service.model.auth.AuthSession;
import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.service.model.enums.SessionStatus;
import com.spring.securitytutorial.service.model.token.IssuedAccessTokenMetadata;
import com.spring.securitytutorial.service.model.token.SessionIdleResponse;
import com.spring.securitytutorial.service.model.user.UserAccount;
import com.spring.securitytutorial.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionJpaRepository sessions;
    private final UserService userRepository;
    private final SecurityProperties properties;
    private final Clock clock;

    @Transactional
    public AuthSession create(UserAccount user, String ipAddress, String userAgent) {
        Instant now = clock.instant();
        sessions.revokeActiveByUser(user.id(), RevokeReason.NEW_LOGIN, now);
        AuthSession session = new AuthSession(
                UUID.randomUUID(), user.id(), SessionStatus.ACTIVE, null, now,
                now.plus(properties.session().absoluteTimeout()), now, null,
                ipAddress, userAgent, null, null, 0);
        insert(session);
        return session;
    }

    @Transactional(noRollbackFor = InvalidSessionException.class)
    public AuthSession validateAndTouch(UUID sessionId, UUID userId) {
        Instant now = clock.instant();
        AuthSession session = findById(sessionId)
                .orElseThrow(() -> new InvalidSessionException("Session not found"));
        if (session.status() != SessionStatus.ACTIVE) {
            throw new InvalidSessionException("Session is not active");
        }
        if (!session.userId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to token subject");
        }
        if (!session.expiresAt().isAfter(now)) {
            changeStatus(sessionId, SessionStatus.EXPIRED, RevokeReason.ABSOLUTE_TIMEOUT, now);
            throw new InvalidSessionException("Session absolute timeout elapsed");
        }
        if (!session.lastActivityAt().plus(properties.session().idleTimeout()).isAfter(now)) {
            changeStatus(sessionId, SessionStatus.EXPIRED, RevokeReason.IDLE_TIMEOUT, now);
            throw new InvalidSessionException("Session idle timeout elapsed");
        }
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidSessionException("Session user not found"));
        if (!user.enabled()) {
            changeStatus(sessionId, SessionStatus.REVOKED, RevokeReason.USER_DISABLED, now);
            throw new InvalidSessionException("User is disabled");
        }
        touchIfOlderThan(
                sessionId, now, now.minus(properties.session().activityUpdateInterval()));
        return findById(sessionId).orElseThrow();
    }

    @Transactional(noRollbackFor = InvalidSessionException.class)
    public SessionIdleResponse idle(UUID sessionId, UUID userId) {
        AuthSession session = validateAndTouch(sessionId, userId);
        return new SessionIdleResponse(
                session.lastActivityAt().plus(properties.session().idleTimeout()),
                properties.session().warningBefore().toSeconds());
    }

    public void recordAccessToken(UUID sessionId, IssuedAccessTokenMetadata token) {
        sessions.updateAccessToken(sessionId, token.jti(), token.expiresAt());
    }

    public void logout(UUID sessionId) {
        changeStatus(sessionId, SessionStatus.LOGOUT, null, clock.instant());
    }

    public void revoke(UUID sessionId, RevokeReason reason) {
        changeStatus(sessionId, SessionStatus.REVOKED, reason, clock.instant());
    }

    public AuthSession requireActive(UUID sessionId) {
        AuthSession session = findById(sessionId)
                .orElseThrow(() -> new InvalidSessionException("Session not found"));
        if (session.status() != SessionStatus.ACTIVE) {
            throw new InvalidSessionException("Session is not active");
        }
        return session;
    }

    public void revokeActiveByUser(UUID userId, RevokeReason reason, Instant now) {
        sessions.revokeActiveByUser(userId, reason, now);
    }

    public List<AuthSession> findAll() {
        return sessions.findAllByOrderByLoginAtDesc().stream().map(this::toDomain).toList();
    }

    public Page<AuthSession> findAll(Pageable pageable) {
        return sessions.findAllByOrderByLoginAtDesc(pageable).map(this::toDomain);
    }

    @Transactional
    public int expireElapsed(Instant now) {
        return sessions.expireElapsed(now);
    }

    public int purgeBefore(Instant threshold) {
        return sessions.purgeBefore(threshold);
    }

    public void insert(AuthSession value) {
        sessions.save(toEntity(value));
    }

    public Optional<AuthSession> findById(UUID id) {
        return sessions.findById(id).map(this::toDomain);
    }

    public int changeStatus(UUID id, SessionStatus status, RevokeReason reason, Instant now) {
        return sessions.changeStatus(id, status, reason, now);
    }

    public int touchIfOlderThan(UUID id, Instant now, Instant threshold) {
        return sessions.touchIfOlderThan(id, now, threshold);
    }

    private SessionEntity toEntity(AuthSession s) {
        SessionEntity e = new SessionEntity();
        e.setId(s.id());
        e.setUserId(s.userId());
        e.setStatus(s.status());
        e.setRevokeReason(s.revokeReason());
        e.setLoginAt(s.loginAt());
        e.setExpiresAt(s.expiresAt());
        e.setLastActivityAt(s.lastActivityAt());
        e.setLogoutAt(s.logoutAt());
        e.setIpAddress(s.ipAddress());
        e.setUserAgent(s.userAgent());
        e.setCurrentAccessTokenJti(s.currentAccessTokenJti());
        e.setAccessTokenExpiresAt(s.accessTokenExpiresAt());
        e.setVersion(s.version());
        return e;
    }

    private AuthSession toDomain(SessionEntity e) {
        return new AuthSession(e.getId(), e.getUserId(), e.getStatus(), e.getRevokeReason(), e.getLoginAt(),
                e.getExpiresAt(), e.getLastActivityAt(), e.getLogoutAt(), e.getIpAddress(), e.getUserAgent(),
                e.getCurrentAccessTokenJti(), e.getAccessTokenExpiresAt(), e.getVersion());
    }
}