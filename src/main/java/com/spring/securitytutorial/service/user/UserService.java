package com.spring.securitytutorial.service.user;

import com.spring.securitytutorial.entity.UserEntity;
import com.spring.securitytutorial.entity.UserRoleEntity;
import com.spring.securitytutorial.service.model.user.UserAccount;
import com.spring.securitytutorial.repository.RoleJpaRepository;
import com.spring.securitytutorial.repository.UserJpaRepository;
import com.spring.securitytutorial.repository.UserRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final UserRoleJpaRepository userRoles;

    public Optional<UserAccount> findByUsername(String username) {
        return users.findByUsername(username).map(this::toDomain);
    }

    public Optional<UserAccount> findById(UUID id) {
        return users.findById(id).map(this::toDomain);
    }

    public List<UserAccount> findAll() {
        return users.findAllByOrderByUsernameAsc().stream().map(this::toDomain).toList();
    }

    public Page<UserAccount> findAll(Pageable pageable) {
        return users.findAll(pageable).map(this::toDomain);
    }

    public void recordSuccessfulLogin(UUID userId, Instant now) {
        users.recordSuccessfulLogin(userId, now);
    }

    @Transactional
    public void insert(UUID id, String username, String passwordHash, Instant now) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        entity.setEnabled(true);
        entity.setCreatedAt(now);
        users.save(entity);
    }

    @Transactional
    public void assignRole(UUID userId, String roleCode) {
        var user = users.findById(userId).orElseThrow();
        var role = roles.findByCodeAndEnabledTrue(roleCode).orElseThrow();
        UserRoleEntity relation = new UserRoleEntity();
        relation.setUser(user);
        relation.setRole(role);
        relation.setEnabled(true);
        userRoles.save(relation);
    }

    public int disable(UUID userId) {
        return users.disable(userId);
    }

    private UserAccount toDomain(UserEntity entity) {
        return new UserAccount(entity.getId(), entity.getUsername(), entity.getPasswordHash(), entity.isEnabled(),
                new LinkedHashSet<>(users.findActiveRoleCodes(entity.getId())),
                new LinkedHashSet<>(users.findActivePermissionCodes(entity.getId())));
    }
}