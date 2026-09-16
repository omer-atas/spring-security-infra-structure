package com.spring.securitytutorial.mapper.admin;

import com.spring.securitytutorial.service.model.auth.AuthSession;
import com.spring.securitytutorial.service.model.admin.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminServiceMapper {

    SessionResponse toSessionResponse(AuthSession session);
}