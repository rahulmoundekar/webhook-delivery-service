package com.rahul.webhook.repository;

import com.rahul.webhook.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookRepository
        extends JpaRepository<Webhook, Long> {

    Optional<Webhook> findByTargetUrl(String targetUrl);
}