package com.spring.securitytutorial.controller;

import com.spring.securitytutorial.controller.model.response.admin.AdminUserApiResponse;
import com.spring.securitytutorial.controller.model.response.admin.SessionApiResponse;
import com.spring.securitytutorial.mapper.admin.AdminApiMapper;
import com.spring.securitytutorial.service.model.enums.PermissionCode;
import com.spring.securitytutorial.service.user.SecurityAdminService;
import com.spring.securitytutorial.util.annotation.HasPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SecurityAdminController {
    private final SecurityAdminService service;
    private final AdminApiMapper mapper;

    @GetMapping("/users")
    @HasPermission(PermissionCode.USER_LIST)
    public Page<AdminUserApiResponse> users(@PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC)
                                           Pageable pageable) {
        return service.listUsers(pageable).map(mapper::toUserResponse);
    }

    @GetMapping("/sessions")
    @HasPermission(PermissionCode.SESSION_LIST)
    public Page<SessionApiResponse> sessions(@PageableDefault(size = 20, sort = "loginAt", direction = Sort.Direction.DESC)
                                            Pageable pageable) {
        return service.listSessions(pageable).map(mapper::toSessionResponse);
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @HasPermission(PermissionCode.SESSION_REVOKE)
    public ResponseEntity<Void> revoke(@PathVariable UUID sessionId) {
        service.revokeSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}