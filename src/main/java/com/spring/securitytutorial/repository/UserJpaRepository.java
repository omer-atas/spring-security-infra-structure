package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findAllByOrderByUsernameAsc();

    @Query(value = """
            SELECT r.code FROM role r JOIN user_role ur ON ur.role_id = r.id
             WHERE ur.user_id = :userId AND r.enabled = TRUE AND ur.enabled = TRUE ORDER BY r.code
            """, nativeQuery = true)
    List<String> findActiveRoleCodes(@Param("userId") UUID userId);

    @Query(value = """
            SELECT DISTINCT p.code FROM permission p
            JOIN role_permission rp ON rp.permission_id = p.id AND rp.enabled = TRUE
            JOIN role r ON r.id = rp.role_id AND r.enabled = TRUE
            JOIN user_role ur ON ur.role_id = r.id AND ur.enabled = TRUE
            JOIN app_user u ON u.id = ur.user_id AND u.enabled = TRUE
            WHERE u.id = :userId AND p.enabled = TRUE ORDER BY p.code
            """, nativeQuery = true)
    List<String> findActivePermissionCodes(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserEntity u SET u.firstLoginAt = COALESCE(u.firstLoginAt, :now), u.lastLoginAt = :now WHERE u.id = :id")
    int recordSuccessfulLogin(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE UserEntity u SET u.enabled = false WHERE u.id = :id AND u.enabled = true")
    int disable(@Param("id") UUID id);
}
