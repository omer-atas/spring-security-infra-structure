package com.spring.securitytutorial.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.RSAKey;
import com.spring.securitytutorial.config.properties.JwtSecurityProperties;
import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.service.security.JwkSetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class RsaKeyConfiguration {

    @Bean
    JwkSetService jwkSetService(JwtSecurityProperties jwt, Environment environment) {
        JwkSetService service = new JwkSetService();
        if (jwt.rsaPrivateKeyPem() != null && jwt.rsaPublicKeyPem() != null) {
            service.register(parseRsaKeyPair(jwt.rsaPrivateKeyPem(), jwt.rsaPublicKeyPem()));
        } else if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            throw new IllegalStateException(
                    "RSA key pair must be configured in prod: app.security.jwt.rsa-private-key-pem and rsa-public-key-pem");
        } else {
            service.register(generateKeyPair());
        }
        return service;
    }

    private KeyPair parseRsaKeyPair(String privateKeyPem, String publicKeyPem) {
        try {
            RSAPublicKey publicKey;
            try (var in = new ByteArrayInputStream(publicKeyPem.getBytes(StandardCharsets.UTF_8))) {
                publicKey = RsaKeyConverters.x509().convert(in);
            }
            RSAPrivateKey privateKey;
            try (var in = new ByteArrayInputStream(privateKeyPem.getBytes(StandardCharsets.UTF_8))) {
                privateKey = RsaKeyConverters.pkcs8().convert(in);
            }
            return new KeyPair(publicKey, privateKey);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse configured RSA key pair", exception);
        }
    }

    private KeyPair generateKeyPair() {
        try {
            java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate development RSA key pair", exception);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(JwkSetService jwkSetService) {
        RSAKey signingKey = jwkSetService.getSigningKey();
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(signingKey));
        return new NimbusJwtEncoder(source);
    }

    @Bean
    JwtDecoder jwtDecoder(JwkSetService jwkSetService, SecurityProperties properties) {
        List<RSAKey> allKeys = jwkSetService.getAllKeys();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(
                        (RSAPublicKey) jwkSetService.getActiveKeyPair().getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(properties.jwt().issuer()),
                new JwtClaimValidator<List<String>>("aud",
                        values -> values != null && values.contains(properties.jwt().audience()))));

        return decoder;
    }
}
