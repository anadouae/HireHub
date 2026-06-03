package com.hirehub.event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private String sourceService;

    @Column(nullable = true)
    private String destinationService;

    @Column(nullable = true)
    private String status; // SUCCESS, FAILED, PENDING

    @Column(nullable = true)
    private String errorMessage;
}