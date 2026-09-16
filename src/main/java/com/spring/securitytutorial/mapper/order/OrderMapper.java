package com.spring.securitytutorial.mapper.order;

import com.spring.securitytutorial.controller.model.request.order.CreateOrderApiRequest;
import com.spring.securitytutorial.controller.model.response.order.OrderApiResponse;
import com.spring.securitytutorial.service.model.order.CreateOrderRequest;
import com.spring.securitytutorial.service.model.order.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    CreateOrderRequest toRequest(CreateOrderApiRequest request);

    OrderApiResponse toResponse(OrderResponse response);

    List<OrderApiResponse> toResponseList(List<OrderResponse> responses);
}