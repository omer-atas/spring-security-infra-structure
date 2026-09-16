package com.spring.securitytutorial.service.order;

import com.spring.securitytutorial.entity.OrderEntity;
import com.spring.securitytutorial.mapper.order.OrderMapper;
import com.spring.securitytutorial.repository.OrderJpaRepository;
import com.spring.securitytutorial.service.model.order.CreateOrderRequest;
import com.spring.securitytutorial.service.model.order.OrderResponse;
import com.spring.securitytutorial.service.security.CurrentCustomerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderJpaRepository repository;
    private final OrderMapper mapper;
    private final CurrentCustomerProvider currentCustomerProvider;
    private final Clock clock;

    @Transactional
    public OrderResponse create(CreateOrderRequest command, Authentication authentication) {
        OrderEntity entity = new OrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(currentCustomerProvider.getRequiredCustomerId());
        entity.setDescription(command.description());
        entity.setStatus("CREATED");
        entity.setCreatedBy(authentication.getName());
        entity.setCreatedAt(clock.instant());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        OrderEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (entity.getCustomerId() == null
                || currentCustomerProvider.getCurrentUserId().filter(entity.getCustomerId()::equals).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(OrderEntity e) {
        return new OrderResponse(e.getId(), e.getCustomerId(), e.getDescription(), e.getStatus(),
                e.getCreatedBy(), e.getCreatedAt());
    }
}