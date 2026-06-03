package com.hirehub.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Audit passif des événements (logging + persistance).
 * Utilise des queues {@code audit.*} dédiées — jamais les queues {@code notif.*} réservées à email-service.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EventAuditListener {

    private final EventLogService eventLogService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUDIT_CANDIDATURE)
    public void auditCandidatureCreated(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement candidature.created reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUDIT_STATUT)
    public void auditStatutChanged(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement candidature.statut.changed reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUDIT_ENTRETIEN)
    public void auditEntretienPlanifie(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement entretien.planifie reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUDIT_RECRUITER)
    public void auditRecruiterDecision(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement recruteur reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUDIT_AUTHENTIFICATION)
    public void auditAuthentication(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement authentification reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUDIT_ADMIN_USER)
    public void auditAdminAction(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement admin (user blocked/deleted) reçu");
    }

    private void auditCommon(EmailEventDTO message, String logPrefix) {
        try {
            if (message.getCorrelationId() != null) {
                MDC.put("correlationId", message.getCorrelationId());
            }
            String eventId = message.getEventId();
            String eventType = message.getEventType();
            String json = safeToJson(message);

            log.info("{}: eventId={}, eventType={}", logPrefix, eventId, eventType);

            eventLogService.logEvent(
                    eventId,
                    eventType,
                    json,
                    "UNKNOWN_SOURCE",
                    "event-service"
            );
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit de l'événement {}: {}", message.getEventType(), e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    private String safeToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
