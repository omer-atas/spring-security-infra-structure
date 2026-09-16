package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByCodeAndEnabledTrue(String code);

    Optional<RoleEntity> findByCode(String code);
}
