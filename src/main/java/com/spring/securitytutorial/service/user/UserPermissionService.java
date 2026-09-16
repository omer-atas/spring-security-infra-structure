package com.spring.securitytutorial.service.user;

import com.spring.securitytutorial.mapper.user.UserServiceMapper;
import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.service.auth.SessionService;
import com.spring.securitytutorial.service.model.user.CreateUserRequest;
import com.spring.securitytutorial.service.model.user.UserResponse;
import com.spring.securitytutorial.util.constant.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UserPermissionService {
    private final UserService users;
    private final SessionService sessions;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final UserServiceMapper mapper;

    public List<UserResponse> list() {
        return users.findAll().stream().map(mapper::toResponse).toList();
    }

    public Page<UserResponse> listPage(Pageable pageable) {
        return users.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public UserResponse create(CreateUserRequest command) {
        UUID id = UUID.randomUUID();
        users.insert(id, command.username(), passwordEncoder.encode(command.password()), clock.instant());
        users.assignRole(id, Roles.USER);
        return mapper.toResponse(users.findById(id).orElseThrow());
    }

    @Transactional
    public boolean disable(UUID userId) {
        boolean changed = users.disable(userId) == 1;
        if (changed) sessions.revokeActiveByUser(userId, RevokeReason.USER_DISABLED, clock.instant());
        return changed;
    }
}
