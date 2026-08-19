package com.rahul.webhook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "webhooks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_webhook_url",
                        columnNames = "target_url"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_url", nullable = false, length = 2000)
    private String targetUrl;

    @Column(name = "secret", nullable = false, length = 500)
    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();

        if (status == null) {
            status = WebhookStatus.ACTIVE;
        }
    }
}