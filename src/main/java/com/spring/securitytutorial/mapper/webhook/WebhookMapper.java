package com.spring.securitytutorial.mapper.webhook;

import com.spring.securitytutorial.controller.model.request.webhook.WebhookApiRequest;
import com.spring.securitytutorial.controller.model.response.webhook.WebhookApiResponse;
import com.spring.securitytutorial.service.model.webhook.WebhookRequest;
import com.spring.securitytutorial.service.model.webhook.WebhookResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookMapper {

    WebhookRequest toRequest(WebhookApiRequest request);

    WebhookApiResponse toResponse(WebhookResponse response);
}