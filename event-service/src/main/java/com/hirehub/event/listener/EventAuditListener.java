package com.hirehub.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Audit passif des événements (logging + persistance).
 * Consomme le contrat standard EmailEventDTO.
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

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void auditStatutChanged(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement candidature.statut.changed reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void auditEntretienPlanifie(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement entretien.planifie reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER)
    public void auditRecruiterDecision(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement recruteur (approved/rejected) reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION)
    public void auditAuthentication(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement authentification reçu");
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER)
    public void auditAdminAction(@Payload EmailEventDTO message) {
        auditCommon(message, "[AUDIT] Événement admin (user blocked/deleted) reçu");
    }

    private void auditCommon(EmailEventDTO message, String logPrefix) {
        try {
            String eventId = message.getEventId();
            String eventType = message.getEventType();
            String json = safeToJson(message);

            log.info("{}: eventId={}, eventType={}", logPrefix, eventId, eventType);

            // Les services source/destination ne sont pas dans EmailEventDTO, on met des placeholders
            eventLogService.logEvent(
                    eventId,
                    eventType,
                    json,
                    "UNKNOWN_SOURCE",
                    "UNKNOWN_DESTINATION"
            );
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit de l'événement {}: {}", message.getEventType(), e.getMessage(), e);
        }
    }

    private String safeToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
