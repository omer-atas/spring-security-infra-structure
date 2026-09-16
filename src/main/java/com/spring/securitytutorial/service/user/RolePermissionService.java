package com.spring.securitytutorial.service.user;

import com.spring.securitytutorial.mapper.role.RolePermissionServiceMapper;
import com.spring.securitytutorial.service.model.role.RolePermissionRow;
import com.spring.securitytutorial.repository.RolePermissionJpaRepository;
import com.spring.securitytutorial.service.model.role.RolePermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionService {
    private final RolePermissionJpaRepository relations;
    private final RolePermissionServiceMapper mapper;

    public List<RolePermissionResponse> listActiveRelations() {
        return mapper.toResponseList(findActiveRelations());
    }

    public List<RolePermissionRow> findActiveRelations() {
        return relations.findActiveRelations().stream()
                .map(projection -> new RolePermissionRow(projection.getRole(), projection.getPermission()))
                .toList();
    }
}