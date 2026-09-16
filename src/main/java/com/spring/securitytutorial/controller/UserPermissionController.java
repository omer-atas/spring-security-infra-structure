package com.spring.securitytutorial.controller;

import com.spring.securitytutorial.controller.model.request.user.CreateUserApiRequest;
import com.spring.securitytutorial.controller.model.response.user.UserApiResponse;
import com.spring.securitytutorial.mapper.user.UserApiMapper;
import com.spring.securitytutorial.service.model.enums.PermissionCode;
import com.spring.securitytutorial.service.user.UserPermissionService;
import com.spring.securitytutorial.util.annotation.HasPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPermissionController {
    private final UserPermissionService service;
    private final UserApiMapper mapper;

    @PostMapping("/list")
    @HasPermission(PermissionCode.USER_LIST)
    public List<UserApiResponse> list() {
        return mapper.toResponseList(service.list());
    }

    @PostMapping("/create")
    @HasPermission(PermissionCode.USER_CREATE)
    public UserApiResponse create(@Valid @RequestBody CreateUserApiRequest createUserApiRequest) {
        return mapper.toResponse(service.create(mapper.toRequest(createUserApiRequest)));
    }

    @DeleteMapping("/{userId}")
    @HasPermission(PermissionCode.USER_DELETE)
    public ResponseEntity<Void> disable(@PathVariable UUID userId) {
        return service.disable(userId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}