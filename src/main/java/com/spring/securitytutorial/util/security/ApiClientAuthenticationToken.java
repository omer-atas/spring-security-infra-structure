package com.spring.securitytutorial.util.security;

import com.spring.securitytutorial.service.model.api.ApiClient;
import com.spring.securitytutorial.service.model.enums.ApiClientType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class ApiClientAuthenticationToken extends AbstractAuthenticationToken {
    private final String key;
    private String secret;
    private final ApiClientType type;
    private final String displayName;
    private final UUID currentUserId;
    private final String signingSecret;

    private ApiClientAuthenticationToken(String key, String secret, ApiClientType type, String displayName,
                                         UUID currentUserId, String signingSecret,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.key = key;
        this.secret = secret;
        this.type = type;
        this.displayName = displayName;
        this.currentUserId = currentUserId;
        this.signingSecret = signingSecret;
        setAuthenticated(authorities != null);
    }

    public static ApiClientAuthenticationToken unauthenticated(String key, String secret, ApiClientType type,
                                                               UUID currentUserId) {
        return new ApiClientAuthenticationToken(key, secret, type, key, currentUserId, null, null);
    }

    public static ApiClientAuthenticationToken authenticated(ApiClient client,
                                                             UUID currentUserId, Collection<? extends GrantedAuthority> authorities) {
        return new ApiClientAuthenticationToken(client.key(), null, client.type(), client.name(),
                currentUserId, client.signingSecret(), authorities);
    }

    @Override
    public Object getCredentials() {
        return secret;
    }

    @Override
    public Object getPrincipal() {
        return displayName;
    }

    public String key() {
        return key;
    }

    public ApiClientType type() {
        return type;
    }

    public UUID currentUserId() {
        return currentUserId;
    }

    public String signingSecret() {
        return signingSecret;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        secret = null;
    }
}