package com.hirehub.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité pour tracer les événements d'email traités.
 * Assure l'idempotence : empêche l'envoi en double d'emails.
 */
@Entity
@Table(name = "email_events_processed", indexes = {
        @Index(name = "idx_event_id", columnList = "event_id", unique = true),
        @Index(name = "idx_recipient_email", columnList = "recipient_email"),
        @Index(name = "idx_processed_at", columnList = "processed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEventProcessed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId; // UUID unique de l'événement

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // Type d'événement (ex: CANDIDATURE.CREATED)

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, PENDING, RETRY

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
    }

}

