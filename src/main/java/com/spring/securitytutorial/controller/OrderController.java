package com.spring.securitytutorial.controller;

import com.spring.securitytutorial.controller.model.request.order.CreateOrderApiRequest;
import com.spring.securitytutorial.controller.model.response.order.OrderApiResponse;
import com.spring.securitytutorial.mapper.order.OrderMapper;
import com.spring.securitytutorial.service.model.enums.PermissionCode;
import com.spring.securitytutorial.service.model.order.OrderResponse;
import com.spring.securitytutorial.service.order.OrderService;
import com.spring.securitytutorial.util.annotation.HasPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;
    private final OrderMapper mapper;

    @PostMapping
    @HasPermission(PermissionCode.ORDER_CREATE)
    public OrderApiResponse create(@Valid @RequestBody CreateOrderApiRequest createOrderApiRequest, Authentication authentication) {
        OrderResponse orderResponse = service.create(mapper.toRequest(createOrderApiRequest), authentication);
        return mapper.toResponse(orderResponse);
    }

    @GetMapping("/{id}")
    @HasPermission(PermissionCode.ORDER_READ)
    public OrderApiResponse get(@PathVariable UUID id) {
        OrderResponse orderResponse = service.get(id);
        return mapper.toResponse(orderResponse);
    }

    @GetMapping("/admin")
    @HasPermission(PermissionCode.ORDER_LIST)
    public List<OrderApiResponse> listAll() {
        List<OrderResponse> orderResponses = service.listAll();
        return mapper.toResponseList(orderResponses);
    }
}