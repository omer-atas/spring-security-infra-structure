package com.spring.securitytutorial.service.security;

import com.spring.securitytutorial.entity.SecurityAuditEntity;
import com.spring.securitytutorial.repository.SecurityAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {
    private final SecurityAuditJpaRepository repository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String eventType, String principal, UUID sessionId, String detail) {
        SecurityAuditEntity entity = new SecurityAuditEntity();
        entity.setEventType(eventType);
        entity.setPrincipal(principal);
        entity.setSessionId(sessionId);
        entity.setDetail(detail);
        entity.setCreatedAt(clock.instant());
        repository.save(entity);
    }

    @Transactional
    public int purgeBefore(Instant threshold) {
        return repository.purgeBefore(threshold);
    }
}
