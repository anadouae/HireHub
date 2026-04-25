package com.hirehub.notification.listener;

import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.common.dtos.events.EventMessage;
import com.hirehub.notification.BusinessMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**<pre>
 * ╔══════════════════════════════════════════════════════════════╗
 * ║    Entretien Planifié Listener - Notifications par email     ║
 * ║                                                              ║
 * ║  Consomme: entretien.planifie                                ║
 * ║  Action: Envoyer email avec date/heure/lieu de l'entretien   ║
 * ║                                                              ║
 * ║  Exemple de message:                                         ║
 * ║  {                                                           ║
 * ║    "entretienId": 15,                                        ║
 * ║    "candidateEmail": "jean@example.com",                     ║
 * ║    "candidateName": "Jean Dupont",                           ║
 * ║    "offerTitle": "Dev Java Senior",                          ║
 * ║    "dateEntretien": "2026-04-15T10:30:00",                   ║
 * ║    "lieu": "Bureau Paris, 3e étage, Salle A",                ║
 * ║    "interviewerName": "Marie Durand"                         ║
 * ║  }                                                           ║
 * ╚══════════════════════════════════════════════════════════════╝
 * </pre>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntretienPlanifieListener {

    private final BusinessMailService emailService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienPlanifie(@Payload EventMessage message) {
        try {
            Map<String, Object> payload = message.getPayload();

            String email = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String offerTitle = (String) payload.get("offerTitle");
            String dateEntretien = (String) payload.get("dateEntretien");
            String lieu = (String) payload.get("lieu");
            String interviewerName = (String) payload.get("interviewerName");

            log.info("[ENTRETIEN] Email de planification envoyé à: {} pour le: {}", email, dateEntretien);

            // TODO: Implémenter emailService.sendEntretienPlanificationEmail(email, candidateName, offerTitle, dateEntretien, lieu, interviewerName);

        } catch (Exception e) {
            log.error("[❌ ENTRETIEN] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement entretien planifié", e);
        }
    }
}

