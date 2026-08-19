package com.rahul.webhook.worker;

import com.rahul.webhook.entity.DeliveryStatus;
import com.rahul.webhook.entity.WebhookDelivery;
import com.rahul.webhook.queue.WebhookQueuePublisher;
import com.rahul.webhook.repository.WebhookDeliveryRepository;
import com.rahul.webhook.security.WebhookSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebhookDeliveryWorker {

    private static final String QUEUE_NAME = "webhook:delivery:queue";

    private static final int MAX_RETRIES = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookSignatureService signatureService;
    private final WebhookQueuePublisher webhookQueuePublisher;
    private final RestClient restClient = RestClient.create();

    // We'll implement the scheduled polling in the next small step.

    @Transactional
    public void processDelivery(Long deliveryId) {

        WebhookDelivery delivery = deliveryRepository.findDeliveryForProcessing(deliveryId).orElse(null);

        if (delivery == null) {
            return;
        }

        if (delivery.getStatus() == DeliveryStatus.DELIVERED || delivery.getStatus() == DeliveryStatus.DEAD_LETTER) {
            return;
        }

        var event = delivery.getEvent();
        var webhook = event.getWebhook();

        delivery.setStatus(DeliveryStatus.DELIVERING);
        delivery.setLastAttemptAt(LocalDateTime.now());
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);

        deliveryRepository.save(delivery);

        String signature = signatureService.generateSignature(event.getPayload(), webhook.getSecret());

        try {

            ResponseEntity<String> response = restClient.post().uri(webhook.getTargetUrl()).header("Content-Type", "application/json").header("X-Webhook-Event", event.getEventType()).header("X-Webhook-Signature", signature).body(event.getPayload()).retrieve().toEntity(String.class);

            int statusCode = response.getStatusCode().value();

            delivery.setLastHttpStatus(statusCode);

            if (statusCode >= 200 && statusCode < 300) {

                delivery.setStatus(DeliveryStatus.DELIVERED);

                delivery.setLastError(null);

            } else {

                handleFailure(delivery, "HTTP status: " + statusCode);
            }

        } catch (Exception ex) {

            handleFailure(delivery, ex.getMessage());
        }

        deliveryRepository.save(delivery);
    }

    private void handleFailure(WebhookDelivery delivery, String error) {

        delivery.setLastError(error);

        if (delivery.getAttemptCount() >= MAX_RETRIES) {

            delivery.setStatus(DeliveryStatus.DEAD_LETTER);

            delivery.setNextAttemptAt(null);

            return;
        }

        delivery.setStatus(DeliveryStatus.RETRYING);

        long delaySeconds = calculateBackoffSeconds(delivery.getAttemptCount());

        delivery.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds));
    }

    private long calculateBackoffSeconds(int attempt) {

        long baseDelay = 1L << attempt;

        long jitter = (long) (Math.random() * 3);

        return baseDelay + jitter;
    }

    @Scheduled(fixedDelay = 1000)
    public void pollQueue() {

        String deliveryId = redisTemplate.opsForList().leftPop(QUEUE_NAME);

        if (deliveryId == null) {
            return;
        }

        processDelivery(Long.parseLong(deliveryId));
    }

    @Scheduled(fixedDelay = 1000)
    public void pollRetries() {
        enqueueDueRetries();
    }

    @Transactional
    public void enqueueDueRetries() {

        List<WebhookDelivery> deliveries = deliveryRepository.findDueRetries(DeliveryStatus.RETRYING, LocalDateTime.now());

        for (WebhookDelivery delivery : deliveries) {

            delivery.setStatus(DeliveryStatus.PENDING);

            delivery.setNextAttemptAt(null);

            deliveryRepository.save(delivery);

            webhookQueuePublisher.enqueueAfterCommit(delivery.getId());
        }
    }
}