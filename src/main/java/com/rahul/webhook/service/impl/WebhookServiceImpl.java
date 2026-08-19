package com.rahul.webhook.service.impl;

import com.rahul.webhook.dto.*;
import com.rahul.webhook.entity.*;
import com.rahul.webhook.exception.ResourceNotFoundException;
import com.rahul.webhook.queue.WebhookQueueService;
import com.rahul.webhook.repository.WebhookDeliveryRepository;
import com.rahul.webhook.repository.WebhookEventRepository;
import com.rahul.webhook.repository.WebhookRepository;
import com.rahul.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookQueueService webhookQueueService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public CreateWebhookResponse createWebhook(CreateWebhookRequest request) {

        webhookRepository.findByTargetUrl(request.targetUrl()).ifPresent(existing -> {
            throw new IllegalArgumentException("Webhook already registered");
        });

        String secret = generateSecret();

        Webhook webhook = Webhook.builder().targetUrl(request.targetUrl()).secret(secret).status(WebhookStatus.ACTIVE).build();

        webhook = webhookRepository.save(webhook);

        return new CreateWebhookResponse(webhook.getId(), webhook.getTargetUrl(), webhook.getStatus().name(), webhook.getSecret());
    }

    @Override
    @Transactional
    public WebhookEventResponse createEvent(Long webhookId, CreateWebhookEventRequest request) {

        Webhook webhook = webhookRepository.findById(webhookId).orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + webhookId));

        if (webhook.getStatus() != WebhookStatus.ACTIVE) {
            throw new IllegalArgumentException("Webhook is not active");
        }

        WebhookEvent event = WebhookEvent.builder().webhook(webhook).eventType(request.eventType()).payload(request.payload()).build();

        event = webhookEventRepository.save(event);

        WebhookDelivery delivery = WebhookDelivery.builder().event(event).status(DeliveryStatus.PENDING).attemptCount(0).build();

        delivery = webhookDeliveryRepository.save(delivery);

        webhookQueueService.enqueue(delivery.getId());

        return new WebhookEventResponse(event.getId(), delivery.getId(), webhook.getId(), event.getEventType(), delivery.getStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDeliveryResponse> getDeliveries(Long webhookId, int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }

        webhookRepository.findById(webhookId).orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + webhookId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<WebhookDelivery> deliveries = webhookDeliveryRepository.findByWebhookId(webhookId, pageable);

        return deliveries.map(delivery -> {

            WebhookEvent event = delivery.getEvent();

            return new WebhookDeliveryResponse(delivery.getId(), event.getId(), event.getEventType(), delivery.getStatus().name(), delivery.getAttemptCount(), delivery.getLastHttpStatus(), delivery.getLastError(), delivery.getNextAttemptAt(), delivery.getLastAttemptAt(), delivery.getCreatedAt());
        });
    }

    @Override
    @Transactional
    public void retryDelivery(Long deliveryId) {

        WebhookDelivery delivery = webhookDeliveryRepository.findById(deliveryId).orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryId));

        if (delivery.getStatus() != DeliveryStatus.DEAD_LETTER) {
            throw new IllegalArgumentException("Only DEAD_LETTER deliveries can be manually retried");
        }

        delivery.setStatus(DeliveryStatus.PENDING);
        delivery.setNextAttemptAt(null);
        delivery.setLastError(null);
        delivery.setLastHttpStatus(null);

        webhookDeliveryRepository.save(delivery);

        webhookQueueService.enqueue(delivery.getId());
    }


    private String generateSecret() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}