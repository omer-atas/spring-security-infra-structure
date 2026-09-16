package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity t SET t.usedAt = :usedAt, t.replacedByTokenId = :replacementId WHERE t.id = :id AND t.usedAt IS NULL AND t.revokedAt IS NULL")
    int markUsed(@Param("id") UUID id, @Param("usedAt") Instant usedAt, @Param("replacementId") UUID replacementId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :now WHERE t.sessionId = :sessionId AND t.revokedAt IS NULL")
    int revokeBySession(@Param("sessionId") UUID sessionId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :now WHERE t.familyId = :familyId AND t.revokedAt IS NULL")
    int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.expiresAt < :threshold AND (t.usedAt IS NOT NULL OR t.revokedAt IS NOT NULL)")
    int purgeBefore(@Param("threshold") Instant threshold);
}
