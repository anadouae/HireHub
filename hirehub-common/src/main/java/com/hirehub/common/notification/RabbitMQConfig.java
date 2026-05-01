package com.hirehub.common.notification;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║           RabbitMQ Configuration - HireHub                    ║
 * ║                                                                ║
 * ║  Cette classe configure l'infrastructure RabbitMQ centralisée:║
 * ║  - L'exchange (aiguilleur central)                           ║
 * ║  - Les queues (boîtes aux lettres)                           ║
 * ║  - Les bindings (connexions entre exchange et queues)        ║
 * ║                                                                ║
 * ║  Place: hirehub-common pour que TOUS les services            ║
 * ║         utilisent exactement la même configuration           ║
 * ║                                                                ║
 * ║  Principe: Configuration = infrastructure                    ║
 * ║            Logic = dans chaque service consumer              ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    // ═══════════════════════════════════════════════════════════════
    // 1-) CONFIGURATION DE L'EXCHANGE (aiguilleur central)
    // ═══════════════════════════════════════════════════════════════

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

    /**
     * Queue pour les NOUVELLES CANDIDATURES
     * Événements: candidature.created
     * Consumer: NotificationService (envoie email de confirmation)
     */
    @Bean
    public Queue notificationCandidatureQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE,
            true,      // durable
            false,     // exclusive
            false      // autoDelete
        );
    }

    @Bean
    public Queue auditCandidatureQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_AUDIT_CANDIDATURE);
        return new Queue(
            RabbitMQConstants.QUEUE_AUDIT_CANDIDATURE,
            true,
                false,
                false
        );
    }

    /**
     * Queue pour les CHANGEMENTS DE STATUT
     * Événements: candidature.statut.changed
     * Consumer: NotificationService
     */
    @Bean
    public Queue notificationStatutQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_STATUT);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_STATUT,
            true, false, false
        );
    }

    /**
     * Queue pour les ENTRETIENS PLANIFIÉS/ANNULÉS
     * Événements: entretien.planifie
     * Consumer: NotificationService
     */
    @Bean
    public Queue notificationEntretienQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN,
            true, false, false
        );
    }

    /**
     * Queue pour les DÉCISIONS RECRUTEUR
     * Événements: recruiter.request.approved, recruiter.request.rejected
     * Consumer: NotificationService
     */
    @Bean
    public Queue notificationRecruiterQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER,
            true, false, false
        );
    }

    /**
     * Queue pour les ACTIONS ADMIN sur les utilisateurs
     * Événements: user.blocked, user.deleted
     * Consumer: NotificationService ou audit-service
     */
    @Bean
    public Queue notificationAdminUserQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER,
            true, false, false
        );
    }

    /**
     * Queue pour les ÉVÉNEMENTS D'AUTHENTIFICATION
     * Événements: user.authentification.register, login, logout, etc.
     * Consumer: NotificationService (emails OTP, confirmations, etc.)
     */
    @Bean
    public Queue notificationAuthenticationQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION);
        return new Queue(
            RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION,
            true, false, false
        );
    }

    /**
     * Queue pour la VÉRIFICATION RECRUTEUR (contrôle OCR/API)
     * Événements: recruiter.registered
     * Consumer: verification-service
     */
    @Bean
    public Queue verificationRecruiterQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER);
        return new Queue(
            RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER,
            true, false, false
        );
    }

    /**
     * Queue pour l'AUTH en réception du résultat de vérification
     * Événements: recruiter.verified
     * Consumer: auth-service
     */
    @Bean
    public Queue authRecruiterVerifiedQueue() {
        log.info("[QUEUE] Création: {}", RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED);
        return new Queue(
            RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED,
            true, false, false
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // 3-  CONFIGURATION DES BINDINGS
    //     (connexions entre exchange et queues)
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Binding bindingCandidatureCreated(
            @Qualifier("notificationCandidatureQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
            RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_CANDIDATURE_CREATED);
    }

    @Bean
    public Binding bindingCandidatureCreatedAudit(
            @Qualifier("auditCandidatureQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
                RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                RabbitMQConstants.QUEUE_AUDIT_CANDIDATURE);
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(RabbitMQConstants.ROUTING_CANDIDATURE_CREATED);
    }

    @Bean
    public Binding bindingStatutChanged(
            @Qualifier("notificationStatutQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED,
            RabbitMQConstants.QUEUE_NOTIFICATION_STATUT);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED);
    }

    @Bean
    public Binding bindingEntretienPlanifie(
            @Qualifier("notificationEntretienQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE,
            RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE);
    }

    @Bean
    public Binding bindingRecruiterApproved(
            @Qualifier("notificationRecruiterQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_RECRUITER_APPROVED,
            RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_RECRUITER_APPROVED);
    }

    @Bean
    public Binding bindingRecruiterRejected(
            @Qualifier("notificationRecruiterQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_RECRUITER_REJECTED,
            RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_RECRUITER_REJECTED);
    }

    @Bean
    public Binding bindingUserBlocked(
            @Qualifier("notificationAdminUserQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_USER_BLOCKED,
            RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_USER_BLOCKED);
    }

    @Bean
    public Binding bindingUserDeleted(
            @Qualifier("notificationAdminUserQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_USER_DELETED,
            RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_USER_DELETED);
    }

    @Bean
    public Binding bindingAuthenticationRegister(
            @Qualifier("notificationAuthenticationQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_REGISTER,
            RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_REGISTER);
    }

    @Bean
    public Binding bindingAuthenticationLogin(
            @Qualifier("notificationAuthenticationQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGIN,
            RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGIN);
    }

    @Bean
    public Binding bindingAuthenticationLogout(
            @Qualifier("notificationAuthenticationQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGOUT,
            RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGOUT);
    }

    @Bean
    public Binding bindingRecruiterRegistered(
            @Qualifier("verificationRecruiterQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_RECRUITER_REGISTERED,
            RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_RECRUITER_REGISTERED);
    }

    @Bean
    public Binding bindingRecruiterVerified(
            @Qualifier("authRecruiterVerifiedQueue") Queue queue,
            TopicExchange exchange
    ) {
        log.info("[BINDING] {} -> {}",
            RabbitMQConstants.ROUTING_RECRUITER_VERIFIED,
            RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED);
        return BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(RabbitMQConstants.ROUTING_RECRUITER_VERIFIED);
    }
}

