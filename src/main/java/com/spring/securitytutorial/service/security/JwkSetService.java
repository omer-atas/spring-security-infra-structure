package com.spring.securitytutorial.service.security;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class JwkSetService {

    private final CopyOnWriteArrayList<KeyRecord> keys = new CopyOnWriteArrayList<>();
    private final Duration keyRotationInterval;
    private final Duration keyRetentionPeriod;

    public JwkSetService() {
        this(Duration.ofDays(7), Duration.ofDays(30));
    }

    public JwkSetService(Duration keyRotationInterval, Duration keyRetentionPeriod) {
        this.keyRotationInterval = keyRotationInterval;
        this.keyRetentionPeriod = keyRetentionPeriod;
    }

    public void register(KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String keyId = UUID.randomUUID().toString();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .build();
        keys.add(new KeyRecord(keyId, keyPair, rsaKey, Instant.now()));
    }

    public void rotateIfNeeded() {
        if (keys.isEmpty()) {
            return;
        }
        KeyRecord latest = keys.get(keys.size() - 1);
        if (Instant.now().isAfter(latest.createdAt().plus(keyRotationInterval))) {
            KeyPair newKeyPair = generateKeyPair();
            register(newKeyPair);
        }
    }

    public RSAKey getSigningKey() {
        rotateIfNeeded();
        return keys.isEmpty() ? null : keys.get(keys.size() - 1).rsaKey();
    }

    public List<RSAKey> getPublicKeys() {
        return keys.stream()
                .filter(k -> Instant.now().isBefore(k.createdAt().plus(keyRetentionPeriod)))
                .map(k -> new RSAKey.Builder((RSAPublicKey) k.keyPair().getPublic())
                        .keyID(k.keyId())
                        .build())
                .sorted(Comparator.comparing(RSAKey::getKeyID))
                .toList();
    }

    public List<RSAKey> getAllKeys() {
        return keys.stream()
                .map(k -> k.rsaKey())
                .sorted(Comparator.comparing(RSAKey::getKeyID))
                .toList();
    }

    public KeyPair getActiveKeyPair() {
        rotateIfNeeded();
        return keys.isEmpty() ? null : keys.get(keys.size() - 1).keyPair();
    }

    public void evictExpired() {
        Instant threshold = Instant.now().minus(keyRetentionPeriod);
        keys.removeIf(k -> k.createdAt().isBefore(threshold) && keys.size() > 1);
    }

    private KeyPair generateKeyPair() {
        try {
            java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate RSA key pair", exception);
        }
    }

    private record KeyRecord(String keyId, KeyPair keyPair, RSAKey rsaKey, Instant createdAt) {
    }
}
