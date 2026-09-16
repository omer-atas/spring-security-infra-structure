package com.spring.securitytutorial.service;

import com.spring.securitytutorial.config.properties.LoginThrottleProperties;
import com.spring.securitytutorial.service.security.LoginThrottleService;
import com.spring.securitytutorial.util.security.RedisFallbackTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginThrottleServiceTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final LoginThrottleProperties PROPERTIES =
            new LoginThrottleProperties(3, Duration.ofMinutes(5));

    private final MutableClock clock = new MutableClock();
    private final RedisFallbackTemplate redisTemplate = createMockRedisTemplate();
    private final LoginThrottleService service = new LoginThrottleService(PROPERTIES, redisTemplate, clock);

    @BeforeEach
    void reset() {
        clock.now = START;
        store.clear();
    }

    @Test
    void allowsAttemptsBelowLimit() {
        assertTrue(service.retryAfter("1.2.3.4", "alice").isEmpty());
        registerFailures("1.2.3.4", "alice", 2);
        assertTrue(service.retryAfter("1.2.3.4", "alice").isEmpty());
    }

    @Test
    void blocksAfterLimitExceeded() {
        registerFailures("1.2.3.4", "alice", 3);
        Optional<Duration> retry = service.retryAfter("1.2.3.4", "alice");
        assertTrue(retry.isPresent());
        assertTrue(retry.get().toSeconds() >= 1);
    }

    @Test
    void usernameKeyBlocksAcrossDifferentIps() {
        service.registerFailure("1.2.3.4", "alice");
        service.registerFailure("5.6.7.8", "alice");
        service.registerFailure("9.9.9.9", "alice");
        assertTrue(service.retryAfter("10.0.0.1", "alice").isPresent());
    }

    @Test
    void ipKeyBlocksOtherUsernamesOnSameIp() {
        service.registerFailure("1.2.3.4", "alice");
        service.registerFailure("1.2.3.4", "bob");
        service.registerFailure("1.2.3.4", "carol");
        assertTrue(service.retryAfter("1.2.3.4", "dave").isPresent());
    }

    @Test
    void successfulLoginClearsKey() {
        registerFailures("1.2.3.4", "alice", 3);
        assertTrue(service.retryAfter("1.2.3.4", "alice").isPresent());
        service.clear("1.2.3.4", "alice");
        assertTrue(service.retryAfter("1.2.3.4", "alice").isEmpty());
    }

    @Test
    void retryAfterReturnsRemainingWindow() {
        registerFailures("1.2.3.4", "alice", 3);
        clock.advance(Duration.ofMinutes(1));
        Optional<Duration> retry = service.retryAfter("1.2.3.4", "alice");
        assertTrue(retry.isPresent());
        assertTrue(retry.get().toSeconds() <= 4 * 60);
        assertTrue(retry.get().toSeconds() >= 1);
    }

    @Test
    void evictExpiredRemovesStaleFailures() {
        registerFailures("1.2.3.4", "alice", 3);
        clock.advance(Duration.ofMinutes(6));
        service.evictExpired();
        assertTrue(service.retryAfter("1.2.3.4", "alice").isEmpty());
    }

    private void registerFailures(String ip, String username, int count) {
        for (int i = 0; i < count; i++) {
            service.registerFailure(ip, username);
        }
    }

    private static final Map<String, String> store = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static RedisFallbackTemplate createMockRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(stringRedisTemplate.keys(anyString())).thenAnswer(invocation -> {
            String pattern = invocation.getArgument(0).toString().replace("*", "");
            return store.keySet().stream()
                    .filter(k -> k.startsWith(pattern))
                    .collect(java.util.stream.Collectors.toSet());
        });

        RedisFallbackTemplate template = mock(RedisFallbackTemplate.class);
        when(template.isRedisAvailable()).thenReturn(true);
        when(template.getRedisTemplate()).thenReturn(stringRedisTemplate);

        when(template.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return store.get(key);
        });

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            store.put(key, value);
            return null;
        }).when(template).set(anyString(), anyString(), any(Duration.class));

        when(template.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            store.remove(key);
            return true;
        });

        when(template.hasKey(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return store.containsKey(key);
        });

        return template;
    }

    private static final class MutableClock extends Clock {
        private Instant now = START;
        private final ZoneId zone = ZoneId.of("UTC");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
