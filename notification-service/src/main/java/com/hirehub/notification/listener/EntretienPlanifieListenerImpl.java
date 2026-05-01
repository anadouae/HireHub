package com.hirehub.notification.listener;

import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.notification.EmailBusinessServiceImpl;
import com.hirehub.notification.service.IdempotenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener pour les événements d'entretien.
 * Consomme les événements RabbitMQ et envoie les emails de planification/annulation.
 * Utilise l'idempotence pour éviter les envois en double.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntretienPlanifieListenerImpl {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    /**
     * Écoute les événements de planification d'entretien.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienPlanifie(@Payload EmailEventDTO event) {
        try {
            String eventId = event.getEventId();

            // Vérifier l'idempotence
            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[ENTRETIEN.PLANIFIE] Événement {} déjà traité, abandon", eventId);
                return;
            }

            log.info("[ENTRETIEN.PLANIFIE] Traitement de l'événement {} pour: {}", eventId, event.getRecipientEmail());

            // Extraire les données du payload
            String offerTitle = (String) event.getPayload().get("offerTitle");
            String interviewDate = (String) event.getPayload().get("interviewDate");
            String interviewLocation = (String) event.getPayload().get("interviewLocation");
            String interviewerName = (String) event.getPayload().get("interviewerName");

            emailService.sendEntretienPlanification(
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    offerTitle,
                    interviewDate,
                    interviewLocation,
                    interviewerName
            );

            // Marquer comme traité avec succès
            idempotenceService.markAsProcessed(eventId, event.getEventType(), event.getRecipientEmail());
            log.info("[✅ ENTRETIEN.PLANIFIE] Email de planification envoyé à: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[❌ ENTRETIEN.PLANIFIE] Erreur lors du traitement: {}", e.getMessage(), e);
            idempotenceService.markAsFailed(event.getEventId(), event.getEventType(), event.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Erreur traitement entretien planifié", e);
        }
    }

    /**
     * Écoute les événements d'annulation d'entretien.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienAnnulation(@Payload EmailEventDTO event) {
        try {
            String eventId = event.getEventId();

            // Vérifier l'idempotence
            if (idempotenceService.isAlreadyProcessed(eventId)) {
                log.warn("[ENTRETIEN.ANNULATION] Événement {} déjà traité, abandon", eventId);
                return;
            }

            log.info("[ENTRETIEN.ANNULATION] Traitement de l'événement {} pour: {}", eventId, event.getRecipientEmail());

            // Extraire les données du payload
            String offerTitle = (String) event.getPayload().get("offerTitle");
            String comment = (String) event.getPayload().get("comment"); // Raison de l'annulation

            emailService.sendEntretienAnnulation(
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    offerTitle,
                    comment
            );

            // Marquer comme traité avec succès
            idempotenceService.markAsProcessed(eventId, event.getEventType(), event.getRecipientEmail());
            log.info("[✅ ENTRETIEN.ANNULATION] Email d'annulation envoyé à: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[❌ ENTRETIEN.ANNULATION] Erreur lors du traitement: {}", e.getMessage(), e);
            idempotenceService.markAsFailed(event.getEventId(), event.getEventType(), event.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Erreur traitement annulation d'entretien", e);
        }
    }

}

