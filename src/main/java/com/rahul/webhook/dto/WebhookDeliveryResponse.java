package com.rahul.webhook.dto;

import java.time.LocalDateTime;

public record WebhookDeliveryResponse(
        Long deliveryId,
        Long eventId,
        String eventType,
        String status,
        int attemptCount,
        Integer lastHttpStatus,
        String lastError,
        LocalDateTime nextAttemptAt,
        LocalDateTime lastAttemptAt,
        LocalDateTime createdAt
) {
}