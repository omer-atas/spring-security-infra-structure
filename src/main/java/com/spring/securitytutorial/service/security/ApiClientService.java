package com.spring.securitytutorial.service.security;

import com.spring.securitytutorial.service.model.api.ApiClient;
import com.spring.securitytutorial.service.model.enums.ApiClientType;
import com.spring.securitytutorial.repository.ApiClientJpaRepository;
import com.spring.securitytutorial.repository.ApiClientAuthorityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiClientService {
    private final ApiClientJpaRepository clients;
    private final ApiClientAuthorityJpaRepository authorities;

    public Optional<ApiClient> findByKeyAndType(String key, ApiClientType type) {
        return clients.findByKeyAndType(key, type).map(entity -> new ApiClient(entity.getId(), entity.getName(),
                entity.getType(), entity.getKey(), entity.getSecretHash(), entity.getSigningSecret(),
                entity.isEnabled(), entity.isCurrentUserRequired(),
                new LinkedHashSet<>(authorities.findAuthorities(entity.getId()))));
    }
}