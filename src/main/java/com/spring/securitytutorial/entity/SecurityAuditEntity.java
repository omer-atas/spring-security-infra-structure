package com.spring.securitytutorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_audit")
@Getter
@Setter
@NoArgsConstructor
public class SecurityAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    private String principal;
    @Column(name = "session_id")
    private UUID sessionId;
    private String detail;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
