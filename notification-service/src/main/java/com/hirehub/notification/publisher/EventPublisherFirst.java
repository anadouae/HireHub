package com.hirehub.notification.publisher;

import com.hirehub.common.constants.RabbitMQConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║         RabbitMQ Publisher - Exemple d'utilisation            ║
 * ║                                                                ║
 * ║  Cette classe montre comment PUBLIER des messages             ║
 * ║  vers RabbitMQ depuis n'importe quel service.                ║
 * ║                                                                ║
 * ║  UTILISATION DEPUIS UN AUTRE SERVICE (ex: candidature-service)║
 * ║  ============================================================  ║
 * ║                                                                ║
 * ║  @Autowired                                                   ║
 * ║  private RabbitTemplate rabbitTemplate;                       ║
 * ║                                                                ║
 * ║  // Dans ta méthode de création de candidature:              ║
 * ║  public void createCandidature(Candidature candidature) {    ║
 * ║      // ... sauvegarder en DB ...                            ║
 * ║                                                                ║
 * ║      // Publier l'événement                                  ║
 * ║      CandidatureCreatedEvent event = new                     ║
 * ║        CandidatureCreatedEvent(                              ║
 * ║          candidature.getId(),                                ║
 * ║          candidature.getCandidat().getEmail(),               ║
 * ║          candidature.getOffre().getTitle()                   ║
 * ║        );                                                     ║
 * ║                                                                ║
 * ║      rabbitTemplate.convertAndSend(                          ║
 * ║          RabbitMQConstants.EXCHANGE,                          ║
 * ║          RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,       ║
 * ║          event                                                ║
 * ║      );                                                        ║
 * ║  }                                                             ║
 * ║                                                                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherFirst {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publier un événement "Candidature créée"
     * Source: candidature-service
     * Destination: notification-service (via queue notif.candidature.queue)
     *
     * @param event Objet contenant les infos de la candidature créée
     */
    public void publishCandidatureCreated(Object event) {
        log.info("[📤 PUBLISH] candidature.created - Event: {}", event);

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
            event
        );

        log.info("[✅ PUBLISHED] candidature.created");
    }

    /**
     * Publier un événement "Statut de candidature changé"
     * Source: candidature-service
     * Destination: notification-service (via queue notif.statut.queue)
     *
     * @param event Objet contenant le nouveau statut et les détails
     */
    public void publishStatutChanged(Object event) {
        log.info("[📤 PUBLISH] candidature.statut.changed - Event: {}", event);

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED,
            event
        );

        log.info("[✅ PUBLISHED] candidature.statut.changed");
    }

    /**
     * Publier un événement "Entretien planifié"
     * Source: entretien-service
     * Destination: notification-service (via queue notif.entretien.queue)
     *
     * @param event Objet contenant les détails de l'entretien
     */
    public void publishEntretienPlanifie(Object event) {
        log.info("[📤 PUBLISH] entretien.planifie - Event: {}", event);

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE,
            event
        );

        log.info("[✅ PUBLISHED] entretien.planifie");
    }

    /**
     * Publier un événement "Demande recruteur approuvée"
     * Source: auth-service (admin approuve)
     * Destination: notification-service (via queue notif.recruiter.queue)
     *
     * @param event Objet contenant les infos du recruteur approuvé
     */
    public void publishRecruiterApproved(Object event) {
        log.info("[📤 PUBLISH] recruiter.request.approved - Event: {}", event);

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            RabbitMQConstants.ROUTING_RECRUITER_APPROVED,
            event
        );

        log.info("[✅ PUBLISHED] recruiter.request.approved");
    }

    /**
     * Publier un événement "Demande recruteur rejetée"
     * Source: auth-service (admin rejette)
     * Destination: notification-service (via queue notif.recruiter.queue)
     *
     * @param event Objet contenant les infos du recruteur et la raison du rejet
     */
    public void publishRecruiterRejected(Object event) {
        log.info("[📤 PUBLISH] recruiter.request.rejected - Event: {}", event);

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            RabbitMQConstants.ROUTING_RECRUITER_REJECTED,
            event
        );

        log.info("[✅ PUBLISHED] recruiter.request.rejected");
    }

    /**
     * Méthode générique pour publier n'importe quel événement
     * (utile si tu veux de la flexibilité)
     *
     * @param routingKey La routing key (utilisé depuis RabbitMQConstants)
     * @param event L'objet à envoyer (sera sérialisé en JSON automatiquement)
     */
    public void publishEvent(String routingKey, Object event) {
        log.info("[📤 PUBLISH] {} - Event: {}", routingKey, event);

        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            routingKey,
            event
        );

        log.info("[✅ PUBLISHED] {}", routingKey);
    }
}

