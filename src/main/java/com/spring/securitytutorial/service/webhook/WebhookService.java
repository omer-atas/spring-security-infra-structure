package com.spring.securitytutorial.service.webhook;

import com.spring.securitytutorial.service.model.webhook.WebhookRequest;
import com.spring.securitytutorial.service.model.webhook.WebhookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final Clock clock;

    public WebhookResponse accept(WebhookRequest command, String clientName) {
        return new WebhookResponse(command.eventType(), clientName, clock.instant());
    }
}