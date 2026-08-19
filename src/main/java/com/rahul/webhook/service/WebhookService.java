package com.rahul.webhook.service;

import com.rahul.webhook.dto.*;
import org.springframework.data.domain.Page;

public interface WebhookService {

    CreateWebhookResponse createWebhook(
            CreateWebhookRequest request
    );

    WebhookEventResponse createEvent(
            Long webhookId,
            CreateWebhookEventRequest request
    );

    Page<WebhookDeliveryResponse> getDeliveries(
            Long webhookId,
            int page,
            int size
    );

    void retryDelivery(Long deliveryId);
}