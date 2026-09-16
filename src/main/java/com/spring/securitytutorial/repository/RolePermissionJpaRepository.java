package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.RolePermissionEntity;
import com.spring.securitytutorial.service.model.role.RolePermissionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionEntity, Long> {

    @Query(value = """
            SELECT r.code AS role, p.code AS permission FROM role r
            LEFT JOIN role_permission rp ON rp.role_id = r.id AND rp.enabled = TRUE
            LEFT JOIN permission p ON p.id = rp.permission_id AND p.enabled = TRUE
            WHERE r.enabled = TRUE ORDER BY r.code, p.code
            """, nativeQuery = true)
    List<RolePermissionProjection> findActiveRelations();

    @Query("SELECT COUNT(rp) > 0 FROM RolePermissionEntity rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    boolean existsByRoleAndPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Modifying
    @Query(value = """
            UPDATE role_permission SET enabled = FALSE
             WHERE role_id = (SELECT id FROM role WHERE code = :roleCode)
               AND permission_id = (SELECT id FROM permission WHERE code = :permissionCode)
            """, nativeQuery = true)
    int disableRelation(@Param("roleCode") String roleCode, @Param("permissionCode") String permissionCode);
}
