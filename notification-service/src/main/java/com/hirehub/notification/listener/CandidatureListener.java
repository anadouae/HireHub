package com.hirehub.notification.listener;


import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.common.dtos.events.EventMessage;
import com.hirehub.notification.BusinessMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The CandidatureListener class listens to RabbitMQ message queues and processes
 * events related to job applications, such as creation of a candidature or changes
 * in candidature status.
 *
 * It uses {@code RabbitListener} annotations to subscribe to specific queues for receiving
 * relevant events. Once an event is received, it processes the payload and initiates actions
 * such as sending notification emails via the {@code BusinessMailService}.
 */
@Slf4j
@Component
public class CandidatureListener {

    private final BusinessMailService emailService;

    public CandidatureListener(BusinessMailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION)
    public void handleCandidatureEvent(@Payload EventMessage message) {
        try {
            Map<String, Object> payload = message.getPayload();
            String eventType = (String) payload.get("eventType");
            String email = (String) payload.get("email");

            switch (eventType) {
                case "CANDIDATURE.CREATED":
                    handleCandidatureCreated(payload);
                    break;
                case "CANDIDATURE.STATUT.CHANGED":
                    handleCandidatureStatutChanged(payload);
                    break;
                default:
                    log.warn("[CANDIDATURE] Événement non reconnu: {}", eventType);
            }

            log.info("[AUTH] Événement {} traité pour: {}", eventType, email);

        } catch (Exception e) {
            log.error("[❌ AUTH] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement authentification", e);
        }
    }


    @RabbitListener(queues = "notif.candidature.queue")
    private void handleCandidatureCreated(Map<String, Object> payload) {

        String email = (String) payload.get("candidateEmail");
        String offerTitle = (String) payload.get("offerTitle");

        emailService.sendCandidatureCreatedEmail(email, offerTitle);
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    private void handleCandidatureStatutChanged(Map<String, Object> payload) {
        try {

            String email = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String offerTitle = (String) payload.get("offerTitle");
            String newStatus = (String) payload.get("newStatus");
            String comment = (String) payload.get("comment");

            log.info("[STATUT] Email envoyé à: {} | Nouveau statut: {}", email, newStatus);

            // TODO: Implémenter emailService.sendStatutChangedEmail(email, candidateName, offerTitle, newStatus, comment);

        } catch (Exception e) {
            log.error("[❌ STATUT] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement changement statut", e);
        }
    }

}
