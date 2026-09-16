package com.spring.securitytutorial.mapper.admin;

import com.spring.securitytutorial.controller.model.response.admin.AdminUserApiResponse;
import com.spring.securitytutorial.controller.model.response.admin.SessionApiResponse;
import com.spring.securitytutorial.service.model.admin.SessionResponse;
import com.spring.securitytutorial.service.model.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminApiMapper {

    AdminUserApiResponse toUserResponse(UserResponse user);

    SessionApiResponse toSessionResponse(SessionResponse session);
}