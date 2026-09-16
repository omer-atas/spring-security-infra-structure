package com.spring.securitytutorial.util.security;

import com.spring.securitytutorial.service.model.api.ApiClient;
import com.spring.securitytutorial.service.model.user.UserAccount;
import com.spring.securitytutorial.service.model.enums.ApiClientType;
import com.spring.securitytutorial.repository.ApiClientUserJpaRepository;
import com.spring.securitytutorial.service.security.ApiClientService;
import com.spring.securitytutorial.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiClientAuthenticationProvider implements AuthenticationProvider {
    private final ApiClientService clients;
    private final ApiClientUserJpaRepository clientUsers;
    private final UserService users;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(@NonNull Authentication authentication) {
        ApiClientAuthenticationToken request = (ApiClientAuthenticationToken) authentication;
        ApiClient client = clients.findByKeyAndType(request.key(), request.type())
                .filter(ApiClient::enabled)
                .orElseThrow(() -> new BadCredentialsException("API client credentials are invalid"));
        if (!passwordEncoder.matches(String.valueOf(request.getCredentials()), client.secretHash())) {
            throw new BadCredentialsException("API client credentials are invalid");
        }
        if (request.type() == ApiClientType.TENANT) {
            validateCurrentUser(client, request.currentUserId());
        }
        return ApiClientAuthenticationToken.authenticated(client,
                request.currentUserId(),
                client.authorities().stream().map(SimpleGrantedAuthority::new).toList());
    }

    private void validateCurrentUser(ApiClient client, java.util.UUID currentUserId) {
        if (!client.currentUserRequired()) {
            return;
        }
        if (currentUserId == null) {
            throw new BadCredentialsException("Current user id is required for this tenant");
        }
        if (!clientUsers.existsByClientIdAndUserIdAndEnabledTrue(client.id(), currentUserId)) {
            throw new BadCredentialsException("Current user is not a member of this tenant");
        }
        users.findById(currentUserId)
                .filter(UserAccount::enabled)
                .orElseThrow(() -> new BadCredentialsException("Current user does not exist or is disabled"));
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return ApiClientAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
