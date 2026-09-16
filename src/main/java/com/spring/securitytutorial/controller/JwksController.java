package com.spring.securitytutorial.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.spring.securitytutorial.service.security.JwkSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwkSetService jwkSetService;

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getKeys() {
        List<RSAKey> publicKeys = jwkSetService.getPublicKeys();
        JWKSet jwkSet = new JWKSet(publicKeys.stream()
                .map(rsaKey -> (com.nimbusds.jose.jwk.JWK) rsaKey)
                .toList());
        return ResponseEntity.ok(jwkSet.toJSONObject());
    }
}
