package com.spring.securitytutorial.config;

import com.spring.securitytutorial.config.properties.BootstrapAdminProperties;
import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.entity.PermissionEntity;
import com.spring.securitytutorial.entity.RoleEntity;
import com.spring.securitytutorial.entity.RolePermissionEntity;
import com.spring.securitytutorial.entity.UserEntity;
import com.spring.securitytutorial.entity.UserRoleEntity;
import com.spring.securitytutorial.service.model.enums.PermissionCode;
import com.spring.securitytutorial.util.constant.Roles;
import com.spring.securitytutorial.repository.PermissionJpaRepository;
import com.spring.securitytutorial.repository.RoleJpaRepository;
import com.spring.securitytutorial.repository.RolePermissionJpaRepository;
import com.spring.securitytutorial.repository.UserJpaRepository;
import com.spring.securitytutorial.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final PermissionJpaRepository permissions;
    private final UserRoleJpaRepository userRoles;
    private final RolePermissionJpaRepository rolePermissions;
    private final SecurityProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        BootstrapAdminProperties admin = properties.bootstrapAdmin();
        if (admin == null || admin.username() == null || admin.username().isBlank()
                || admin.password() == null || admin.password().isBlank()) {
            return;
        }
        if (users.findByUsername(admin.username()).isPresent()) {
            return;
        }
        RoleEntity adminRole = roles.findByCode(Roles.ADMIN).orElseGet(this::createRole);
        List<PermissionEntity> allowed = ensurePermissions();
        for (PermissionEntity permission : allowed) {
            ensureRolePermission(adminRole, permission);
        }
        ensureUser(admin, adminRole);
        LOGGER.info("Bootstrap admin ensured for user '{}'", admin.username());
    }

    private RoleEntity createRole() {
        RoleEntity role = new RoleEntity();
        role.setCode(Roles.ADMIN);
        role.setEnabled(true);
        return roles.save(role);
    }

    private List<PermissionEntity> ensurePermissions() {
        List<PermissionEntity> allowed = new ArrayList<>();
        for (PermissionCode code : PermissionCode.values()) {
            PermissionEntity permission = permissions.findByCode(code.name())
                    .orElseGet(() -> {
                        PermissionEntity created = new PermissionEntity();
                        created.setCode(code.name());
                        return permissions.save(created);
                    });
            allowed.add(permission);
        }
        return allowed;
    }

    private void ensureRolePermission(RoleEntity role, PermissionEntity permission) {
        boolean exists = rolePermissions.existsByRoleAndPermission(role.getId(), permission.getId());
        if (exists) {
            return;
        }
        RolePermissionEntity row = new RolePermissionEntity();
        row.setRole(role);
        row.setPermission(permission);
        row.setEnabled(true);
        rolePermissions.save(row);
    }

    private void ensureUser(BootstrapAdminProperties admin, RoleEntity adminRole) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(admin.username());
        user.setPasswordHash(passwordEncoder.encode(admin.password()));
        user.setEnabled(true);
        user.setCreatedAt(clock.instant());
        users.save(user);

        UserRoleEntity assignment = new UserRoleEntity();
        assignment.setUser(user);
        assignment.setRole(adminRole);
        assignment.setEnabled(true);
        userRoles.save(assignment);
    }
}