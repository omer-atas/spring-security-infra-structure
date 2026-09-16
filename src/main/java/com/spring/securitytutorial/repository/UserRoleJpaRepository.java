package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
}
