package com.rahul.webhook.repository;

import com.rahul.webhook.entity.DeliveryStatus;
import com.rahul.webhook.entity.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WebhookDeliveryRepository
        extends JpaRepository<WebhookDelivery, Long> {

    @Query("""
        SELECT d
        FROM WebhookDelivery d
        JOIN FETCH d.event e
        JOIN FETCH e.webhook w
        WHERE d.id = :deliveryId
        """)
    Optional<WebhookDelivery> findDeliveryForProcessing(
            Long deliveryId
    );

    List<WebhookDelivery> findByStatusAndNextAttemptAtLessThanEqual(
            DeliveryStatus status,
            LocalDateTime time
    );

    @Query("""
    SELECT d
    FROM WebhookDelivery d
    JOIN FETCH d.event e
    WHERE e.webhook.id = :webhookId
    """)
    Page<WebhookDelivery> findByWebhookId(
            Long webhookId,
            Pageable pageable
    );

    @Query("""
    SELECT d
    FROM WebhookDelivery d
    WHERE d.status = :status
      AND d.nextAttemptAt <= :now
    """)
    List<WebhookDelivery> findDueRetries(
            DeliveryStatus status,
            LocalDateTime now
    );
}