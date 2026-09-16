package com.spring.securitytutorial.controller;

import com.spring.securitytutorial.controller.model.response.role.RolePermissionApiResponse;
import com.spring.securitytutorial.mapper.role.RolePermissionApiMapper;
import com.spring.securitytutorial.service.model.enums.PermissionCode;
import com.spring.securitytutorial.service.model.role.RolePermissionResponse;
import com.spring.securitytutorial.service.user.RolePermissionService;
import com.spring.securitytutorial.util.annotation.HasPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolePermissionController {
    private final RolePermissionService service;
    private final RolePermissionApiMapper mapper;

    @PostMapping("/list")
    @HasPermission(PermissionCode.USER_ROLES_LIST)
    public List<RolePermissionApiResponse> list() {
        List<RolePermissionResponse> rolePermissionResponses = service.listActiveRelations();
        return mapper.toResponseList(rolePermissionResponses);
    }
}