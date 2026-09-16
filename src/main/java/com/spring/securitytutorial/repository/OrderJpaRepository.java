package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findAllByOrderByCreatedAtDesc();
}
