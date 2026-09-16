package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.SecurityAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SecurityAuditJpaRepository extends JpaRepository<SecurityAuditEntity, Long> {

    long countByEventType(String eventType);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SecurityAuditEntity a WHERE a.createdAt < :threshold")
    int purgeBefore(@Param("threshold") Instant threshold);
}