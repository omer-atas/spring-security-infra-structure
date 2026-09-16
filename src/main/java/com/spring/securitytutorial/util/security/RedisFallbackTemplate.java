package com.spring.securitytutorial.util.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Component
public class RedisFallbackTemplate {

    @Getter
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();
    private volatile boolean redisAvailable = true;

    public RedisFallbackTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    public void setRedisAvailable(boolean available) {
        this.redisAvailable = available;
    }

    public String get(String key) {
        if (!redisAvailable) {
            return localCache.getIfPresent(key);
        }
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                localCache.put(key, value);
            }
            return value;
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to local cache for key: {}", key);
            redisAvailable = false;
            return localCache.getIfPresent(key);
        }
    }

    public void set(String key, String value, Duration ttl) {
        localCache.put(key, value);
        if (!redisAvailable) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis unavailable, using local cache only for key: {}", key);
            redisAvailable = false;
        }
    }

    public Boolean delete(String key) {
        localCache.invalidate(key);
        if (!redisAvailable) {
            return true;
        }
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis unavailable, local cache cleared for key: {}", key);
            redisAvailable = false;
            return true;
        }
    }

    public void deleteByPattern(String pattern) {
        localCache.asMap().keySet().removeIf(k -> k.startsWith(pattern.replace("*", "")));
        if (!redisAvailable) {
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, local cache cleared for pattern: {}", pattern);
            redisAvailable = false;
        }
    }

    public Boolean hasKey(String key) {
        if (!redisAvailable) {
            return localCache.getIfPresent(key) != null;
        }
        try {
            Boolean result = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(result)) {
                localCache.put(key, "1");
            }
            return result;
        } catch (Exception e) {
            log.warn("Redis unavailable, checking local cache for key: {}", key);
            redisAvailable = false;
            return localCache.getIfPresent(key) != null;
        }
    }

    public void healthCheck() {
        try {
            redisTemplate.execute((RedisConnection connection) -> {
                connection.ping();
                return "PONG";
            });
            redisAvailable = true;
        } catch (Exception e) {
            log.warn("Redis health check failed");
            redisAvailable = false;
        }
    }
}
