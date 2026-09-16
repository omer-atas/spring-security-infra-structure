package com.spring.securitytutorial.entity;

import com.spring.securitytutorial.service.model.enums.ApiClientType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "api_client")
@Getter
@Setter
@NoArgsConstructor
public class ApiClientEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private ApiClientType type;
    @Column(name = "client_key", nullable = false, unique = true)
    private String key;
    @Column(name = "secret_hash", nullable = false)
    private String secretHash;
    @Column(name = "signing_secret")
    private String signingSecret;
    @Column(nullable = false)
    private boolean enabled;
    @Column(name = "current_user_required", nullable = false)
    private boolean currentUserRequired;
}