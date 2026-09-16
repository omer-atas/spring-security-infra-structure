package com.spring.securitytutorial.service.security;

import com.spring.securitytutorial.config.properties.LoginThrottleProperties;
import com.spring.securitytutorial.util.security.RedisFallbackTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LoginThrottleService {

    private static final String IP_PREFIX = "login-throttle:ip:";
    private static final String USERNAME_PREFIX = "login-throttle:user:";

    private final LoginThrottleProperties properties;
    private final RedisFallbackTemplate redisTemplate;
    private final Clock clock;

    public Optional<Duration> retryAfter(String ip, String username) {
        Optional<Duration> byIp = retryDelay(IP_PREFIX + ip);
        Optional<Duration> byUser = username == null || username.isBlank()
                ? Optional.empty()
                : retryDelay(USERNAME_PREFIX + username);
        if (byIp.isEmpty()) {
            return byUser;
        }
        if (byUser.isEmpty()) {
            return byIp;
        }
        return byIp.get().compareTo(byUser.get()) >= 0 ? byIp : byUser;
    }

    public void registerFailure(String ip, String username) {
        register(IP_PREFIX + ip);
        if (username != null && !username.isBlank()) {
            register(USERNAME_PREFIX + username);
        }
    }

    public void clearAll() {
        redisTemplate.deleteByPattern(IP_PREFIX + "*");
        redisTemplate.deleteByPattern(USERNAME_PREFIX + "*");
    }

    public void clear(String ip, String username) {
        redisTemplate.delete(IP_PREFIX + ip + ":count");
        redisTemplate.delete(IP_PREFIX + ip + ":oldest");
        if (username != null && !username.isBlank()) {
            redisTemplate.delete(USERNAME_PREFIX + username + ":count");
            redisTemplate.delete(USERNAME_PREFIX + username + ":oldest");
        }
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    public void scheduledEvictExpired() {
        evictExpired();
    }

    public void evictExpired() {
        if (!redisTemplate.isRedisAvailable()) {
            return;
        }
        Instant threshold = clock.instant().minus(properties.window());
        Set<String> ipKeys = redisTemplate.getRedisTemplate().keys(IP_PREFIX + "*");
        if (ipKeys != null) {
            ipKeys.stream()
                    .filter(k -> k.endsWith(":oldest"))
                    .forEach(key -> evictKey(key.substring(0, key.length() - ":oldest".length()), threshold));
        }
        Set<String> userKeys = redisTemplate.getRedisTemplate().keys(USERNAME_PREFIX + "*");
        if (userKeys != null) {
            userKeys.stream()
                    .filter(k -> k.endsWith(":oldest"))
                    .forEach(key -> evictKey(key.substring(0, key.length() - ":oldest".length()), threshold));
        }
    }

    private Optional<Duration> retryDelay(String key) {
        String countStr = redisTemplate.get(key + ":count");
        if (countStr == null) {
            return Optional.empty();
        }
        long count = Long.parseLong(countStr);
        if (count < properties.maxAttempts()) {
            return Optional.empty();
        }
        String oldestStr = redisTemplate.get(key + ":oldest");
        if (oldestStr == null) {
            return Optional.empty();
        }
        Instant oldest = Instant.parse(oldestStr);
        Instant now = clock.instant();
        long retryMillis = oldest.plus(properties.window()).toEpochMilli() - now.toEpochMilli();
        return Optional.of(Duration.ofMillis(Math.max(1_000, retryMillis)));
    }

    private void register(String key) {
        Instant now = clock.instant();
        String countKey = key + ":count";
        String oldestKey = key + ":oldest";

        String countStr = redisTemplate.get(countKey);
        long count = countStr != null ? Long.parseLong(countStr) : 0;

        if (count == 0) {
            redisTemplate.set(oldestKey, now.toString(), properties.window().plusSeconds(60));
        }

        redisTemplate.set(countKey, String.valueOf(count + 1), properties.window().plusSeconds(60));
    }

    private void evictKey(String key, Instant threshold) {
        String oldestStr = redisTemplate.get(key + ":oldest");
        if (oldestStr != null) {
            Instant oldest = Instant.parse(oldestStr);
            if (oldest.isBefore(threshold)) {
                redisTemplate.delete(key + ":count");
                redisTemplate.delete(key + ":oldest");
            }
        }
    }
}
