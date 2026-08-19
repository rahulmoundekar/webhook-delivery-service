package com.rahul.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWebhookRequest(

        @NotBlank(message = "targetUrl is required")
        @Size(max = 2000, message = "targetUrl must not exceed 2000 characters")
        String targetUrl
) {
}