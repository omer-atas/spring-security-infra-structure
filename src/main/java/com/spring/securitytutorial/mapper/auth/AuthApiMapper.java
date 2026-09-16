package com.spring.securitytutorial.mapper.auth;

import com.spring.securitytutorial.controller.model.request.auth.LoginApiRequest;
import com.spring.securitytutorial.controller.model.request.auth.RefreshApiRequest;
import com.spring.securitytutorial.controller.model.response.auth.CurrentUserApiResponse;
import com.spring.securitytutorial.controller.model.response.auth.SessionIdleApiResponse;
import com.spring.securitytutorial.controller.model.response.auth.TokenApiResponse;
import com.spring.securitytutorial.service.model.auth.AuthTokenResponse;
import com.spring.securitytutorial.service.model.auth.CurrentUserResponse;
import com.spring.securitytutorial.service.model.auth.LoginRequest;
import com.spring.securitytutorial.service.model.auth.RefreshTokenRequest;
import com.spring.securitytutorial.service.model.token.SessionIdleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthApiMapper {

    LoginRequest toRequest(LoginApiRequest request);

    RefreshTokenRequest toRequest(RefreshApiRequest request);

    TokenApiResponse toResponse(AuthTokenResponse result);

    CurrentUserApiResponse toResponse(CurrentUserResponse result);

    SessionIdleApiResponse toResponse(SessionIdleResponse result);
}