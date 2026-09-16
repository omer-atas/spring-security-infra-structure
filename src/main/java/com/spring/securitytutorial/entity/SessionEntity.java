package com.spring.securitytutorial.entity;

import com.spring.securitytutorial.service.model.enums.RevokeReason;
import com.spring.securitytutorial.service.model.enums.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
@Getter
@Setter
@NoArgsConstructor
public class SessionEntity {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "revoke_reason")
    private RevokeReason revokeReason;
    @Column(name = "login_at", nullable = false)
    private Instant loginAt;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;
    @Column(name = "logout_at")
    private Instant logoutAt;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "user_agent")
    private String userAgent;
    @Column(name = "current_access_token_jti")
    private UUID currentAccessTokenJti;
    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;
    @Version
    private long version;
}
