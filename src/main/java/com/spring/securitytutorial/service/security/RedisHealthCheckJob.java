package com.spring.securitytutorial.service.security;

import com.spring.securitytutorial.util.security.RedisFallbackTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthCheckJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisHealthCheckJob.class);

    private final RedisFallbackTemplate redisTemplate;

    @Scheduled(fixedDelayString = "PT30S", initialDelayString = "PT10S")
    public void checkRedisHealth() {
        boolean wasAvailable = redisTemplate.isRedisAvailable();
        redisTemplate.healthCheck();
        boolean isAvailable = redisTemplate.isRedisAvailable();

        if (wasAvailable && !isAvailable) {
            LOGGER.warn("Redis connection lost, falling back to local cache");
        } else if (!wasAvailable && isAvailable) {
            LOGGER.info("Redis connection restored");
        }
    }
}
