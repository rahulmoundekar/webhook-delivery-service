package com.rahul.webhook.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisWebhookQueueService
        implements WebhookQueueService {

    private static final String QUEUE_NAME =
            "webhook:delivery:queue";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void enqueue(Long deliveryId) {

        redisTemplate
                .opsForList()
                .rightPush(
                        QUEUE_NAME,
                        String.valueOf(deliveryId)
                );
    }

    
}