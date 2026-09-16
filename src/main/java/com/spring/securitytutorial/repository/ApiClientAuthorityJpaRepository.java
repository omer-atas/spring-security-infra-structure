package com.spring.securitytutorial.repository;

import com.spring.securitytutorial.entity.ApiClientAuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApiClientAuthorityJpaRepository extends JpaRepository<ApiClientAuthorityEntity, Long> {

    @Query("SELECT a.authority FROM ApiClientAuthorityEntity a WHERE a.client.id = :clientId ORDER BY a.authority")
    List<String> findAuthorities(@Param("clientId") UUID clientId);

    boolean existsByClientIdAndAuthority(UUID clientId, String authority);
}
