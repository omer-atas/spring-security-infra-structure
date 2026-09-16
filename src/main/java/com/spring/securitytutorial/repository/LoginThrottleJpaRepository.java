package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.LoginThrottleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LoginThrottleJpaRepository extends JpaRepository<LoginThrottleEntity, Long> {

    List<LoginThrottleEntity> findByThrottleKeyAndAttemptedAtAfterOrderByAttemptedAtAsc(String throttleKey, Instant after);

    @Modifying
    @Query("DELETE FROM LoginThrottleEntity t WHERE t.throttleKey = :throttleKey")
    int deleteByThrottleKey(@Param("throttleKey") String throttleKey);

    @Modifying
    @Query("DELETE FROM LoginThrottleEntity t WHERE t.attemptedAt < :before")
    int deleteBefore(@Param("before") Instant before);
}