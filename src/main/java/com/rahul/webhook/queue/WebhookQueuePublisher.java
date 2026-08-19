package com.rahul.webhook.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class WebhookQueuePublisher {

    private static final String QUEUE_NAME =
            "webhook:delivery:queue";

    private final RedisTemplate<String, String> redisTemplate;

    public void enqueueAfterCommit(Long deliveryId) {

        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            enqueue(deliveryId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        enqueue(deliveryId);
                    }
                }
        );
    }

    private void enqueue(Long deliveryId) {

        redisTemplate
                .opsForList()
                .rightPush(
                        QUEUE_NAME,
                        String.valueOf(deliveryId)
                );
    }
}