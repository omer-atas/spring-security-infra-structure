package com.spring.securitytutorial.mapper.user;

import com.spring.securitytutorial.controller.model.request.user.CreateUserApiRequest;
import com.spring.securitytutorial.controller.model.response.user.UserApiResponse;
import com.spring.securitytutorial.service.model.user.CreateUserRequest;
import com.spring.securitytutorial.service.model.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserApiMapper {

    CreateUserRequest toRequest(CreateUserApiRequest request);

    UserApiResponse toResponse(UserResponse response);

    List<UserApiResponse> toResponseList(List<UserResponse> responses);
}