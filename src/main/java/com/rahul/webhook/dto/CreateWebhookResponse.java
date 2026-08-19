package com.rahul.webhook.dto;

public record CreateWebhookResponse(

        Long id,

        String targetUrl,

        String status,

        String secret
) {
}