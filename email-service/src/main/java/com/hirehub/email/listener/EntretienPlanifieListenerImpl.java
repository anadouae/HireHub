package com.hirehub.email.listener;

import com.hirehub.common.constants.EventType;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.email.EmailBusinessServiceImpl;
import com.hirehub.email.service.IdempotenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener pour les événements d'entretien (planification / annulation).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntretienPlanifieListenerImpl {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienEvent(@Payload EmailEventDTO event) {
        try {
            if (event.getCorrelationId() != null) {
                MDC.put("correlationId", event.getCorrelationId());
            }
            String eventId = event.getEventId();
            String eventType = event.getEventType();

            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[ENTRETIEN] Événement {} déjà traité, abandon", eventId);
                return;
            }

            Map<String, Object> payload = event.getPayload() != null ? event.getPayload() : Map.of();
            String offerTitle = payload.get("offerTitle") != null ? payload.get("offerTitle").toString() : "Offre";

            if (EventType.ENTRETIEN_PLANIFIE.equals(eventType)) {
                log.info("[ENTRETIEN.PLANIFIE] Traitement pour {}", event.getRecipientEmail());
                emailService.sendEntretienPlanification(
                        event.getRecipientEmail(),
                        event.getRecipientName(),
                        offerTitle,
                        str(payload, "interviewDate"),
                        str(payload, "interviewLocation"),
                        str(payload, "interviewerName")
                );
            } else if ("ENTRETIEN.ANNULATION".equals(eventType)) {
                log.info("[ENTRETIEN.ANNULATION] Traitement pour {}", event.getRecipientEmail());
                emailService.sendEntretienAnnulation(
                        event.getRecipientEmail(),
                        event.getRecipientName(),
                        offerTitle,
                        str(payload, "comment")
                );
            } else {
                log.warn("[ENTRETIEN] Type non reconnu: {}", eventType);
                return;
            }

            idempotenceService.markAsProcessed(eventId, eventType, event.getRecipientEmail());
            log.info("[ENTRETIEN.{}] OK - Email envoyé à {}", eventType, event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[ENTRETIEN] ERREUR lors du traitement", e);
            idempotenceService.markAsFailed(event.getEventId(), event.getEventType(), event.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Erreur traitement entretien", e);
        } finally {
            MDC.clear();
        }
    }

    private static String str(Map<String, Object> payload, String key) {
        Object v = payload.get(key);
        return v != null ? v.toString() : "";
    }
}
