package com.spring.securitytutorial.service.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwkKeyRotationJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwkKeyRotationJob.class);

    private final JwkSetService jwkSetService;

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    public void rotateKeys() {
        int before = jwkSetService.getPublicKeys().size();
        jwkSetService.evictExpired();
        jwkSetService.rotateIfNeeded();
        int after = jwkSetService.getPublicKeys().size();
        if (after > before) {
            LOGGER.info("JWK key rotation: {} keys available", after);
        }
    }
}
