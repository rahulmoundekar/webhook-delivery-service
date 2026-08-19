package com.rahul.webhook.dto;

public record WebhookEventResponse(

        Long eventId,

        Long deliveryId,

        Long webhookId,

        String eventType,

        String status
) {
}