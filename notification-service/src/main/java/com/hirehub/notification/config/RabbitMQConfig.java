package com.hirehub.notification.config;

import com.hirehub.common.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║           RabbitMQ Configuration - HireHub                    ║
 * ║                                                                ║
 * ║  Cette classe configure l'infrastructure RabbitMQ:           ║
 * ║  - L'exchange (aiguilleur central)                           ║
 * ║  - Les queues (boîtes aux lettres)                           ║
 * ║  - Les bindings (connexions entre exchange et queues)        ║
 * ║                                                                ║
 * ║  ⚠️  NE PAS modifier cette config directement dans           ║
 * ║      d'autres services — tous les services utilisent         ║
 * ║      LES MÊMES constantes de RabbitMQConstants.              ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    // ═══════════════════════════════════════════════════════════════
    // 1-) CONFIGURATION DE L'EXCHANGE (aiguilleur central)
    // ═══════════════════════════════════════════════════════════════
    //
    // L'exchange est du type "Topic" :
    // - Les publishers envoient des messages avec une routing key
    // - L'exchange les dirige vers les queues en fonction de la routing key
    // - Les consumers s'abonnent aux queues
    //
    // Type Topic = permet les patterns de matching (ex: "candidature.*")
    //

    @Bean
    public TopicExchange hirehubExchange() {
        log.info("[EXCHANGE] Création du TopicExchange: {}", RabbitMQConstants.EXCHANGE);
        return new TopicExchange(
            RabbitMQConstants.EXCHANGE,
            true,      // durable = survive aux redémarrages de RabbitMQ
            false      // autoDelete = ne pas supprimer si plus de queues
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // 2-  CONFIGURATION DES QUEUES (boîtes aux lettres)
    // ═══════════════════════════════════════════════════════════════
    //
    // Chaque queue est consommée par un service spécifique.
    // Les queues persistent les messages jusqu'à qu'un consumer les traite.
    //

    /**
     * Queue pour les notifications de NOUVELLES CANDIDATURES
     * Événements: candidature.created
     * Consumer: NotificationService (envoie email de confirmation)
     */
    @Bean
    public Queue notificationCandidatureQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE,
            true,      // durable
            false,     // exclusive (pas réservée à une connexion)
            false      // autoDelete
        );
    }

    /**
     * Queue pour les notifications de CHANGEMENT DE STATUT
     * Événements: candidature.statut.changed
     * Consumer: NotificationService (envoie email de mise à jour)
     */
    @Bean
    public Queue notificationStatutQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_STATUT);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_STATUT,
            true,
            false,
            false
        );
    }

    /**
     * Queue pour les notifications d'ENTRETIENS PLANIFIÉS/ANNULÉS
     * Événements: entretien.planifie
     * Consumer: NotificationService (envoie email avec détails entretien)
     */
    @Bean
    public Queue notificationEntretienQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN,
            true,
            false,
            false
        );
    }

    /**
     * Queue pour les notifications des DÉCISIONS RECRUTEUR
     * Événements: recruiter.request.approved, recruiter.request.rejected
     * Consumer: NotificationService (envoie email d'approbation/rejet)
     */
    @Bean
    public Queue notificationRecruiterQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER,
            true,
            false,
            false
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // 3-  CONFIGURATION DES BINDINGS
    //     (connexions entre exchange et queues)
    // ═══════════════════════════════════════════════════════════════
    //
    // Un binding dit: "Si un message arrive avec cette routing key,
    // le mettre dans cette queue"
    //
    // Syntax: exchange --[routing_key]--> queue
    //

    /**
     * Binding 1: candidature.created → QUEUE_NOTIFICATION_CANDIDATURE
     *
     * Quand candidature-service publie "candidature.created",
     * le message arrive dans cette queue.
     */
    @Bean
    public Binding bindingCandidatureCreated(
            @Qualifier("notificationCandidatureQueue") Queue notificationCandidatureQueue,
            TopicExchange hirehubExchange
    ) {
        log.info(
                "[BINDING] {} -> {}",
                RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE
        );
        return BindingBuilder
            .bind(notificationCandidatureQueue())
            .to(hirehubExchange)
            .with(RabbitMQConstants.ROUTING_CANDIDATURE_CREATED);
    }

    /**
     * Binding 2: candidature.statut.changed -> QUEUE_NOTIFICATION_STATUT
     *
     * Quand candidature-service publie "candidature.statut.changed",
     * le message arrive dans cette queue.
     */
    @Bean
    public Binding bindingStatutChanged(
            @Qualifier("notificationStatutQueue") Queue notificationStatutQueue,
            TopicExchange hirehubExchange
    ) {
        log.info("✅ [BINDING] {} -> {}", RabbitMQConstants.ROUTING_STATUT_CHANGED, RabbitMQConstants.QUEUE_NOTIFICATION_STATUT);
        return BindingBuilder
            .bind(notificationStatutQueue())
            .to(hirehubExchange)
            .with(RabbitMQConstants.ROUTING_STATUT_CHANGED);
    }

    /**
     * Binding 3: entretien.planifie -> QUEUE_NOTIFICATION_ENTRETIEN
     *
     * Quand entretien-service publie "entretien.planifie",
     * le message arrive dans cette queue.
     */
    @Bean
    public Binding bindingEntretienPlanifie(
            @Qualifier("notificationEntretienQueue") Queue notificationEntretienQueue,
            TopicExchange hirehubExchange
    ) {
        log.info("✅ [BINDING] {} -> {}", RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE, RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN);
        return BindingBuilder
            .bind(notificationEntretienQueue())
            .to(hirehubExchange)
            .with(RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE);
    }

    /**
     * Binding 4a: recruiter.request.approved -> QUEUE_NOTIFICATION_RECRUITER
     *
     * Quand auth-service publie "recruiter.request.approved",
     * le message arrive dans cette queue.
     */
    @Bean
    public Binding bindingRecruiterApproved(
            @Qualifier("notificationRecruiterQueue") Queue notificationRecruiterQueue,
            TopicExchange hirehubExchange
    ) {
        log.info("✅ [BINDING] {} -> {}", RabbitMQConstants.ROUTING_RECRUITER_APPROVED, RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return BindingBuilder
            .bind(notificationRecruiterQueue())
            .to(hirehubExchange)
            .with(RabbitMQConstants.ROUTING_RECRUITER_APPROVED);
    }

    /**
     * Binding 4b: recruiter.request.rejected -> QUEUE_NOTIFICATION_RECRUITER
     *
     * Quand auth-service publie "recruiter.request.rejected",
     * le message arrive AUSSI dans cette queue (même queue pour 2 events).
     *
     * Ceci dmontre la flexibilit du Topic Exchange:
     * - Une queue peut recevoir de plusieurs routing keys
     * - Les consumers doivent identifier le type d'event pour ragir
     */
    @Bean
    public Binding bindingRecruiterRejected(
            @Qualifier("notificationRecruiterQueue") Queue notificationRecruiterQueue,
            TopicExchange hirehubExchange
    ) {
        log.info("✅ [BINDING] {} -> {}", RabbitMQConstants.ROUTING_RECRUITER_REJECTED, RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return BindingBuilder
            .bind(notificationRecruiterQueue())
            .to(hirehubExchange)
            .with(RabbitMQConstants.ROUTING_RECRUITER_REJECTED);
    }

    /*@Bean
    public AmqpTemplate amqpTemplate (ConnectionFactory connectionFactory) {
        RabbitTemplate  rabbitTemplate  =  new  RabbitTemplate (connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter converter () {
        return  new  Jackson2JsonMessageConverter ();
    }*/

    // ═══════════════════════════════════════════════════════════════
    // z RÉSUMÉ DE L'ARCHITECTURE
    // ═══════════════════════════════════════════════════════════════
    //
    //                    Publisher (ex: candidature-service)
    //                              │
    //                              │ rabbitTemplate.convertAndSend(
    //                              │     EXCHANGE,
    //                              │     "candidature.created",
    //                              │     event)
    //                              ▼
    //                    ┌─────────────────────┐
    //                    │  hirehub.events     │
    //                    │  (TopicExchange)    │
    //                    └──────────┬──────────┘
    //                               │
    //        ┌──────────────────────┼──────────────────────┐
    //        │                      │                      │
    //   routing key=         routing key=           routing key=
    //   candidature.created  candidature.          entretien.
    //                        statut.changed        planifie
    //        │                      │                      │
    //        ▼                      ▼                      ▼
    //   ┌─────────────┐        ┌──────────┐         ┌──────────┐
    //   │ notif.      │        │ notif.   │         │ notif.   │
    //   │ candidature │        │ statut   │         │ entretien│
    //   │ queue       │        │ queue    │         │ queue    │
    //   └──────┬──────┘        └────┬─────┘         └────┬─────┘
    //          │                    │                    │
    //          └────────────────────┼────────────────────┘
    //                               │
    //                               ▼
    //                    NotificationService
    //                    (@RabbitListener)
    //                    │
    //                    ├─ envoie email si candidature.created
    //                    ├─ envoie email si statut.changed
    //                    └─ envoie email si entretien.planifie
    //
    // ═══════════════════════════════════════════════════════════════
}
