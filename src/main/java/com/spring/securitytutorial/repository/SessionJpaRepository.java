package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.SessionEntity;
import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.service.model.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {

    List<SessionEntity> findAllByOrderByLoginAtDesc();

    Page<SessionEntity> findAllByOrderByLoginAtDesc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SessionEntity s SET s.status = 'REVOKED', s.revokeReason = :reason, s.logoutAt = :now, s.version = s.version + 1 WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    int revokeActiveByUser(@Param("userId") UUID userId, @Param("reason") RevokeReason reason, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SessionEntity s SET s.status = :status, s.revokeReason = :reason, s.logoutAt = :now, s.version = s.version + 1 WHERE s.id = :id AND s.status = 'ACTIVE'")
    int changeStatus(@Param("id") UUID id, @Param("status") SessionStatus status, @Param("reason") RevokeReason reason, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SessionEntity s SET s.lastActivityAt = :now, s.version = s.version + 1 WHERE s.id = :id AND s.status = 'ACTIVE' AND s.lastActivityAt < :threshold")
    int touchIfOlderThan(@Param("id") UUID id, @Param("now") Instant now, @Param("threshold") Instant threshold);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SessionEntity s SET s.currentAccessTokenJti = :jti, s.accessTokenExpiresAt = :expiresAt, s.version = s.version + 1 WHERE s.id = :id AND s.status = 'ACTIVE'")
    int updateAccessToken(@Param("id") UUID id, @Param("jti") UUID jti, @Param("expiresAt") Instant expiresAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SessionEntity s SET s.status = 'EXPIRED', s.revokeReason = 'ABSOLUTE_TIMEOUT', s.logoutAt = :now, s.version = s.version + 1 WHERE s.status = 'ACTIVE' AND s.expiresAt <= :now")
    int expireElapsed(@Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SessionEntity s WHERE s.status <> 'ACTIVE' AND s.expiresAt < :threshold")
    int purgeBefore(@Param("threshold") Instant threshold);
}
