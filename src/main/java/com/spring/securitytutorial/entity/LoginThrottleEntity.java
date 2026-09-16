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

@Entity
@Table(name = "login_throttle")
@Getter
@Setter
@NoArgsConstructor
public class LoginThrottleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "throttle_key", nullable = false)
    private String throttleKey;
    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;
}