package com.spring.securitytutorial.mapper.user;

import com.spring.securitytutorial.service.model.user.UserAccount;
import com.spring.securitytutorial.service.model.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserServiceMapper {

    UserResponse toResponse(UserAccount account);
}