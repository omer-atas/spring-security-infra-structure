package com.spring.securitytutorial.service.auth;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.service.security.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SessionCleanupJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionCleanupJob.class);

    private final SessionService sessions;
    private final RefreshTokenService refreshTokens;
    private final SecurityAuditService audit;
    private final SecurityProperties properties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    public void expireElapsedSessions() {
        sessions.expireElapsed(clock.instant());
    }

    @Scheduled(fixedDelayString = "PT6H", initialDelayString = "PT1H")
    @Transactional
    public void purgeRetiredRecords() {
        Instant now = clock.instant();
        int removedRefreshTokens = refreshTokens.purgeBefore(now.minus(properties.retention().refreshToken()));
        int removedSessions = sessions.purgeBefore(now.minus(properties.retention().session()));
        int removedAudit = audit.purgeBefore(now.minus(properties.retention().audit()));
        if (removedRefreshTokens + removedSessions + removedAudit > 0) {
            LOGGER.info("Purged retired records: {} refresh tokens, {} sessions, {} audit rows",
                    removedRefreshTokens, removedSessions, removedAudit);
        }
    }
}