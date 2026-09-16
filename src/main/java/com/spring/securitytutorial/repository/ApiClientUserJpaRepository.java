package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.ApiClientUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiClientUserJpaRepository extends JpaRepository<ApiClientUserEntity, Long> {

    boolean existsByClientIdAndUserIdAndEnabledTrue(UUID clientId, UUID userId);
}