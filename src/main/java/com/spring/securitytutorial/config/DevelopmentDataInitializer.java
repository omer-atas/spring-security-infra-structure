package com.spring.securitytutorial.config;

import com.spring.securitytutorial.entity.ApiClientAuthorityEntity;
import com.spring.securitytutorial.entity.ApiClientEntity;
import com.spring.securitytutorial.entity.ApiClientUserEntity;
import com.spring.securitytutorial.entity.PermissionEntity;
import com.spring.securitytutorial.entity.RoleEntity;
import com.spring.securitytutorial.entity.RolePermissionEntity;
import com.spring.securitytutorial.entity.UserEntity;
import com.spring.securitytutorial.entity.UserRoleEntity;
import com.spring.securitytutorial.service.model.enums.ApiClientType;
import com.spring.securitytutorial.util.constant.Roles;
import com.spring.securitytutorial.repository.ApiClientAuthorityJpaRepository;
import com.spring.securitytutorial.repository.ApiClientJpaRepository;
import com.spring.securitytutorial.repository.ApiClientUserJpaRepository;
import com.spring.securitytutorial.repository.PermissionJpaRepository;
import com.spring.securitytutorial.repository.RoleJpaRepository;
import com.spring.securitytutorial.repository.RolePermissionJpaRepository;
import com.spring.securitytutorial.repository.UserJpaRepository;
import com.spring.securitytutorial.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.seed-data-enabled", havingValue = "true")
public class DevelopmentDataInitializer implements ApplicationRunner {
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final PermissionJpaRepository permissions;
    private final UserRoleJpaRepository userRoles;
    private final RolePermissionJpaRepository rolePermissions;
    private final ApiClientJpaRepository clients;
    private final ApiClientAuthorityJpaRepository clientAuthorities;
    private final ApiClientUserJpaRepository clientUsers;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<String> permissionCodes = List.of("PROFILE_READ", "MESSAGE_WRITE", "USER_LIST", "USER_CREATE",
                "USER_DELETE", "USER_ROLES_LIST", "SESSION_LIST", "SESSION_REVOKE",
                "ORDER_CREATE", "ORDER_READ", "ORDER_LIST");
        permissionCodes.forEach(this::ensurePermission);

        createRoleIfMissing(Roles.USER);
        RoleEntity adminRole = createRoleIfMissing(Roles.ADMIN);
        List.of("ORDER_CREATE", "ORDER_READ", "ORDER_LIST")
                .forEach(code -> ensureRolePermission(adminRole, code));

        ensureUser("admin", "admin123", Roles.ADMIN, permissionCodes);
        ensureUser("user", "user123", Roles.USER, List.of("PROFILE_READ"));

        ensureClient("MOBILE_BFF", ApiClientType.TENANT, "mobile-bff-key", "mobile-bff-secret", null, true,
                List.of(Roles.MOBILE_BFF, "MESSAGE_WRITE", "ORDER_CREATE", "ORDER_READ", "ORDER_LIST"),
                List.of("user", "admin"));
        ensureClient("DEMO_WEBHOOK_CLIENT", ApiClientType.WEBHOOK, "demo-webhook", "webhook-secret",
                "webhook-signing-secret", false, List.of(Roles.WEBHOOK_CLIENT), List.of());
    }

    private void ensurePermission(String code) {
        if (permissions.findByCode(code).isEmpty()) {
            PermissionEntity entity = new PermissionEntity();
            entity.setCode(code);
            permissions.save(entity);
        }
    }

    private RoleEntity createRoleIfMissing(String code) {
        return roles.findByCode(code).orElseGet(() -> {
            RoleEntity entity = new RoleEntity();
            entity.setCode(code);
            entity.setEnabled(true);
            return roles.save(entity);
        });
    }

    private void ensureUser(String username, String password, String roleCode, List<String> permissionCodes) {
        if (users.findByUsername(username).isPresent()) {
            return;
        }
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setCreatedAt(clock.instant());
        users.save(user);

        RoleEntity role = roles.findByCode(roleCode).orElseThrow();
        UserRoleEntity assignment = new UserRoleEntity();
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setEnabled(true);
        userRoles.save(assignment);

        permissionCodes.forEach(code -> ensureRolePermission(role, code));
    }

    private void ensureRolePermission(RoleEntity role, String permissionCode) {
        PermissionEntity permission = permissions.findByCode(permissionCode).orElseThrow();
        if (!rolePermissions.existsByRoleAndPermission(role.getId(), permission.getId())) {
            RolePermissionEntity item = new RolePermissionEntity();
            item.setRole(role);
            item.setPermission(permission);
            item.setEnabled(true);
            rolePermissions.save(item);
        }
    }

    private void ensureClient(String name, ApiClientType type, String key, String secret, String signingSecret,
                              boolean currentUserRequired, List<String> authorities, List<String> memberUsernames) {
        ApiClientEntity client = clients.findByKeyAndType(key, type).orElseGet(() -> {
            ApiClientEntity created = new ApiClientEntity();
            created.setId(UUID.randomUUID());
            created.setName(name);
            created.setType(type);
            created.setKey(key);
            created.setSecretHash(passwordEncoder.encode(secret));
            created.setEnabled(true);
            created.setCurrentUserRequired(currentUserRequired);
            return clients.save(created);
        });
        if (client.getSigningSecret() == null && signingSecret != null) {
            client.setSigningSecret(signingSecret);
            clients.save(client);
        }
        authorities.forEach(authority -> {
            if (!clientAuthorities.existsByClientIdAndAuthority(client.getId(), authority)) {
                ApiClientAuthorityEntity item = new ApiClientAuthorityEntity();
                item.setClient(client);
                item.setAuthority(authority);
                clientAuthorities.save(item);
            }
        });
        memberUsernames.forEach(username -> users.findByUsername(username).ifPresent(user -> {
            if (!clientUsers.existsByClientIdAndUserIdAndEnabledTrue(client.getId(), user.getId())) {
                ApiClientUserEntity membership = new ApiClientUserEntity();
                membership.setClientId(client.getId());
                membership.setUserId(user.getId());
                membership.setEnabled(true);
                clientUsers.save(membership);
            }
        }));
    }
}