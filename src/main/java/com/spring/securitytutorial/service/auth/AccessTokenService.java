package com.spring.securitytutorial.service.auth;

import com.spring.securitytutorial.config.properties.SecurityProperties;
import com.spring.securitytutorial.service.model.auth.AuthSession;
import com.spring.securitytutorial.service.model.token.IssuedAccessToken;
import com.spring.securitytutorial.service.model.user.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties properties;
    private final Clock clock;

    public IssuedAccessToken issue(UserAccount user, AuthSession session) {
        Instant issuedAt = clock.instant();
        Instant configuredExpiry = issuedAt.plus(properties.jwt().accessTokenTtl());
        Instant expiresAt = configuredExpiry.isBefore(session.expiresAt()) ? configuredExpiry : session.expiresAt();
        UUID jti = UUID.randomUUID();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .audience(List.of(properties.jwt().audience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.id().toString())
                .id(jti.toString())
                .claim("sid", session.id().toString())
                .claim("username", user.username())
                .claim("token_type", "PORTAL_USER")
                .claim("roles", user.roles())
                .claim("permissions", user.permissions())
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(value, jti, expiresAt);
    }
}
