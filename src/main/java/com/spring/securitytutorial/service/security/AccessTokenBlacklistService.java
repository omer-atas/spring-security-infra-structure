package com.spring.securitytutorial.service.security;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.util.security.RedisFallbackTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "access-token-blacklist:";

    private final RedisFallbackTemplate redisTemplate;
    private final SecurityProperties properties;

    public void blacklist(String jti, Duration remainingTtl) {
        String key = BLACKLIST_PREFIX + jti;
        Duration ttl = remainingTtl.isPositive() ? remainingTtl : properties.accessTokenBlacklist().ttl();
        redisTemplate.set(key, "revoked", ttl);
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
