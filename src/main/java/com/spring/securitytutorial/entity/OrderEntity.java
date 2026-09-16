package com.spring.securitytutorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {
    @Id
    private UUID id;
    @Column(name = "customer_id")
    private UUID customerId;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
