package com.hirehub.common.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO générique pour les événements d'email.
 * Utilisé pour tous les types de notifications via RabbitMQ.
 *
 * Structure:
 * - Metadata : eventId, eventType, timestamp
 * - Recipient : recipientEmail, recipientName
 * - Payload : données métier spécifiques selon eventType
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== METADATA (Commun à tous les événements) =====
    @JsonProperty("eventId")
    private String eventId; // UUID unique pour l'idempotence

    @JsonProperty("eventType")
    private String eventType; // ex: CANDIDATURE.CREATED, AUTH.OTP, AUTH.LOGIN, AUTH.LOGOUT

    @JsonProperty("timestamp")
    private Long timestamp; // Timestamp de création de l'événement (ms)

    // ===== RECIPIENT (Commun à tous les événements) =====
    @JsonProperty("recipientEmail")
    private String recipientEmail;

    @JsonProperty("recipientName")
    private String recipientName;

    // ===== PAYLOAD =====
    @JsonProperty("payload")
    private Map<String, Object> payload;
}
