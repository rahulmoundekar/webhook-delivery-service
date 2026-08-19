package com.rahul.webhook.controller;

import com.rahul.webhook.dto.*;
import com.rahul.webhook.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<CreateWebhookResponse> createWebhook(@Valid @RequestBody CreateWebhookRequest request) {

        CreateWebhookResponse response = webhookService.createWebhook(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{webhookId}/events")
    public ResponseEntity<WebhookEventResponse> createEvent(@PathVariable Long webhookId, @Valid @RequestBody CreateWebhookEventRequest request) {

        WebhookEventResponse response = webhookService.createEvent(webhookId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{webhookId}/deliveries")
    public ResponseEntity<Page<WebhookDeliveryResponse>> getDeliveries(@PathVariable Long webhookId,

                                                                       @RequestParam(defaultValue = "0") int page,

                                                                       @RequestParam(defaultValue = "10") int size) {

        Page<WebhookDeliveryResponse> response = webhookService.getDeliveries(webhookId, page, size);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deliveries/{deliveryId}/retry")
    public ResponseEntity<Void> retryDelivery(@PathVariable Long deliveryId) {

        webhookService.retryDelivery(deliveryId);

        return ResponseEntity.accepted().build();
    }
}