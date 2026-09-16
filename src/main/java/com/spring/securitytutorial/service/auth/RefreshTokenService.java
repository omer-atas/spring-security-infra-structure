package com.spring.securitytutorial.service.auth;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.entity.RefreshTokenEntity;
import com.spring.securitytutorial.exception.InvalidRefreshTokenException;
import com.spring.securitytutorial.service.model.token.IssuedRefreshToken;
import com.spring.securitytutorial.service.model.token.RefreshTokenRecord;
import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.repository.RefreshTokenJpaRepository;
import com.spring.securitytutorial.service.model.token.RefreshRotationResult;
import com.spring.securitytutorial.service.security.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenJpaRepository tokens;
    private final SessionService sessionService;
    private final SecurityAuditService audit;
    private final SecurityProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public IssuedRefreshToken issue(UUID sessionId) {
        return issue(sessionId, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshRotationResult rotate(String rawToken) {
        Instant now = clock.instant();
        RefreshTokenRecord current = findByHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));
        if (current.usedAt() != null || current.revokedAt() != null) {
            throwReuse(current, now);
        }
        if (!current.expiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenException("Refresh token is expired or revoked");
        }
        sessionService.requireActive(current.sessionId());
        IssuedRefreshToken replacement = create(current.sessionId(), current.familyId(), now);
        if (markUsed(current.id(), now, replacement.id()) != 1) {
            throwReuse(current, now);
        }
        insert(new RefreshTokenRecord(
                replacement.id(), current.sessionId(), current.familyId(), hash(replacement.value()),
                now, replacement.expiresAt(), null, null, null));
        return new RefreshRotationResult(current.sessionId(), replacement);
    }

    private void throwReuse(RefreshTokenRecord current, Instant now) {
        revokeFamily(current.familyId(), now);
        sessionService.revoke(current.sessionId(), RevokeReason.REFRESH_TOKEN_REUSE);
        audit.record("REFRESH_TOKEN_REUSE", null, current.sessionId(),
                "familyId=" + current.familyId());
        throw new InvalidRefreshTokenException("Refresh token reuse detected");
    }

    public void revokeSession(UUID sessionId) {
        revokeBySession(sessionId, clock.instant());
    }

    private IssuedRefreshToken issue(UUID sessionId, UUID familyId) {
        Instant now = clock.instant();
        IssuedRefreshToken token = create(sessionId, familyId, now);
        insert(new RefreshTokenRecord(
                token.id(), sessionId, familyId, hash(token.value()), now, token.expiresAt(), null, null, null));
        return token;
    }

    private IssuedRefreshToken create(UUID sessionId, UUID familyId, Instant now) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new IssuedRefreshToken(
                value, UUID.randomUUID(), familyId, now.plus(properties.refreshToken().ttl()));
    }

    private String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public void insert(RefreshTokenRecord token) {
        tokens.save(toEntity(token));
    }

    public Optional<RefreshTokenRecord> findByHash(String hash) {
        return tokens.findByTokenHash(hash).map(this::toDomain);
    }

    public int markUsed(UUID id, Instant usedAt, UUID replacementId) {
        return tokens.markUsed(id, usedAt, replacementId);
    }

    public int revokeBySession(UUID sessionId, Instant now) {
        return tokens.revokeBySession(sessionId, now);
    }

    public int revokeFamily(UUID familyId, Instant now) {
        return tokens.revokeFamily(familyId, now);
    }

    public int purgeBefore(Instant threshold) {
        return tokens.purgeBefore(threshold);
    }

    private RefreshTokenEntity toEntity(RefreshTokenRecord t) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setId(t.id());
        e.setSessionId(t.sessionId());
        e.setFamilyId(t.familyId());
        e.setTokenHash(t.tokenHash());
        e.setCreatedAt(t.createdAt());
        e.setExpiresAt(t.expiresAt());
        e.setUsedAt(t.usedAt());
        e.setRevokedAt(t.revokedAt());
        e.setReplacedByTokenId(t.replacedByTokenId());
        return e;
    }

    private RefreshTokenRecord toDomain(RefreshTokenEntity e) {
        return new RefreshTokenRecord(e.getId(), e.getSessionId(), e.getFamilyId(), e.getTokenHash(),
                e.getCreatedAt(), e.getExpiresAt(), e.getUsedAt(), e.getRevokedAt(), e.getReplacedByTokenId());
    }
}