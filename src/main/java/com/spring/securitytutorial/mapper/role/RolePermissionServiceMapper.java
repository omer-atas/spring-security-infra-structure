package com.spring.securitytutorial.mapper.role;

import com.spring.securitytutorial.service.model.role.RolePermissionRow;
import com.spring.securitytutorial.service.model.role.RolePermissionResponse;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface RolePermissionServiceMapper {

    RolePermissionResponse toResponse(RolePermissionRow row);

    List<RolePermissionResponse> toResponseList(List<RolePermissionRow> rows);
}