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
 * Listener pour les événements de candidature.
 * Consomme les événements RabbitMQ et envoie les emails correspondants.
 * Utilise l'idempotence pour éviter les envois en double.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandidatureListener {

    private final EmailBusinessServiceImpl emailService;
    private final IdempotenceService idempotenceService;

    /**
     * Écoute les événements de création de candidature.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE)
    public void handleCandidatureCreated(@Payload EmailEventDTO event) {
        try {
            String eventId = event.getEventId();
            log.info("[CANDIDATURE.CREATED] Traitement de l'événement {} pour: {}", eventId, event.getRecipientEmail());

            // Extraire les données du payload
            log.info("Contenu complet du payload : {}", event.getPayload());
            //log.info("Valeur de offerId : '{}'", event.getPayload().get("offerId"));

            String offerTitle = (String) event.getPayload().get("offerTitle");
            //Long offerId = ((Long) event.getPayload().get("offerId")).longValue();
            Long offerId = Long.valueOf(event.getPayload().get("offerId").toString());

            emailService.sendCandidatureConfirmation(
                    new com.hirehub.common.dtos.candidatures.CandidatureDTO(
                            event.getRecipientEmail(),
                            event.getRecipientName(),
                            offerTitle,
                            offerId
                    )
            );

            log.info("[✅ CANDIDATURE.CREATED] Email envoyé avec succès à: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[❌ CANDIDATURE.CREATED] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement création candidature", e);
        }
    }

    /**
     * Écoute les événements de changement de statut de candidature.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void handleCandidatureStatutChanged(@Payload EmailEventDTO event) {
        try {
            String eventId = event.getEventId();
            log.info("[CANDIDATURE.STATUT.CHANGED] Traitement de l'événement {} pour: {}", eventId, event.getRecipientEmail());

            // Extraire les données du payload
            String offerTitle = (String) event.getPayload().get("offerTitle");
            String oldStatus = (String) event.getPayload().get("oldStatus");
            String newStatus = (String) event.getPayload().get("newStatus");
            String comment = (String) event.getPayload().get("comment");

            emailService.sendCandidatureStatutChangedNotification(
                    event.getRecipientEmail(),
                    event.getRecipientName(),
                    offerTitle,
                    oldStatus,
                    newStatus,
                    comment
            );

            log.info("[✅ CANDIDATURE.STATUT.CHANGED] Email envoyé avec succès à: {}", event.getRecipientEmail());

        } catch (Exception e) {
            log.error("[❌ CANDIDATURE.STATUT.CHANGED] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement changement statut", e);
        }
    }

}
