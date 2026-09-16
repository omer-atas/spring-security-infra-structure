package com.spring.securitytutorial.service.user;

import com.spring.securitytutorial.mapper.admin.AdminServiceMapper;
import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.service.auth.SessionService;
import com.spring.securitytutorial.service.model.admin.SessionResponse;
import com.spring.securitytutorial.service.model.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityAdminService {
    private final UserPermissionService users;
    private final SessionService sessions;
    private final AdminServiceMapper mapper;

    public Page<UserResponse> listUsers(Pageable pageable) {
        return users.listPage(pageable);
    }

    public Page<SessionResponse> listSessions(Pageable pageable) {
        return sessions.findAll(pageable).map(mapper::toSessionResponse);
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessions.revoke(sessionId, RevokeReason.ADMIN_REVOKE);
    }
}