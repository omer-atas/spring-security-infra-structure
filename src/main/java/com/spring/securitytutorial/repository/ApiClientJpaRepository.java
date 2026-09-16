package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.ApiClientEntity;
import com.spring.securitytutorial.service.model.enums.ApiClientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiClientJpaRepository extends JpaRepository<ApiClientEntity, UUID> {

    Optional<ApiClientEntity> findByKeyAndType(String key, ApiClientType type);
}
