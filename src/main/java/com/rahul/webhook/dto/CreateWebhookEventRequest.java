package com.rahul.webhook.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWebhookEventRequest(

        @NotBlank(message = "eventType is required")
        String eventType,

        @NotBlank(message = "payload is required")
        String payload
) {
}