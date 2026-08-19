package com.rahul.webhook.queue;

public interface WebhookQueueService {

    void enqueue(Long deliveryId);
}