package com.spring.securitytutorial.util.security;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;

@Component
public class WebhookSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PREFIX = "sha256=";

    private final Duration ttl;
    private final Clock clock;

    public WebhookSignatureVerifier(SecurityProperties properties, Clock clock) {
        this.ttl = properties.webhookSignature().ttl();
        this.clock = clock;
    }

    public boolean verify(String secret, String timestampHeader, String signatureHeader, byte[] body) {
        if (secret == null || timestampHeader == null || signatureHeader == null || body == null) {
            return false;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader);
        } catch (NumberFormatException exception) {
            return false;
        }
        long now = clock.instant().getEpochSecond();
        if (Math.abs(now - timestamp) > ttl.getSeconds()) {
            return false;
        }
        if (!signatureHeader.startsWith(PREFIX)) {
            return false;
        }
        String expected = signatureHeader.substring(PREFIX.length());
        String computed = hmacHex(secret, timestamp + "." + new String(body, StandardCharsets.UTF_8));
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                computed.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacHex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(HMAC_ALGORITHM + " is unavailable", exception);
        }
    }
}