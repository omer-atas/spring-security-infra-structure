package com.spring.securitytutorial.controller;

import com.spring.securitytutorial.controller.model.request.webhook.WebhookApiRequest;
import com.spring.securitytutorial.controller.model.response.webhook.WebhookApiResponse;
import com.spring.securitytutorial.mapper.webhook.WebhookMapper;
import com.spring.securitytutorial.service.model.webhook.WebhookRequest;
import com.spring.securitytutorial.service.model.webhook.WebhookResponse;
import com.spring.securitytutorial.service.webhook.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService service;
    private final WebhookMapper mapper;

    @PostMapping("/events")
    public WebhookApiResponse accept(@Valid @RequestBody WebhookApiRequest webhookApiRequest, Authentication authentication) {
        WebhookRequest webhookRequest = mapper.toRequest(webhookApiRequest);
        WebhookResponse webhookResponse = service.accept(webhookRequest, authentication.getName());
        return mapper.toResponse(webhookResponse);
    }
}