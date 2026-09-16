package com.spring.securitytutorial.mapper.role;

import com.spring.securitytutorial.controller.model.response.role.RolePermissionApiResponse;
import com.spring.securitytutorial.service.model.role.RolePermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RolePermissionApiMapper {

    List<RolePermissionApiResponse> toResponseList(List<RolePermissionResponse> responses);
}