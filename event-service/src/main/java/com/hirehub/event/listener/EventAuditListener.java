package com.hirehub.event.listener;

import com.hirehub.common.constants.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║        Event Audit Listener - Tracking & Logging              ║
 * ║                                                                ║
 * ║  Rôle: Audit passif des événements pour logging               ║
 * ║  - Consomme les événements sans action métier                ║
 * ║  - Persiste les événements en DB (EventLog table)            ║
 * ║  - Expose les métriques                                      ║
 * ║                                                                ║
 * ║  ⚠️  N'interfère PAS avec les autres consumers                ║
 * ║  Chaque queue peut avoir plusieurs consumers                  ║
 * ║  (event-service pour audit, notification-service pour mail)   ║
 * ║                                                                ║
 * ║  Pattern: "competing consumers" dans RabbitMQ                ║
 * ║  = plusieurs services écoutent la même queue                 ║
 * ║  = chacun reçoit une copie du message                        ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EventAuditListener {

    /**
     * Audit des événements "candidature créée"
     * Consomme en parallèle avec notification-service
     * (chacun a sa propre queue = deux copies du message)
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE)
    public void auditCandidatureCreated(@Payload String message) {
        try {
            log.info("[AUDIT] Événement candidature.created reçu: {}", message);
            // TODO: Persister en EventLog table
            // TODO: Incrémenter les métriques
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit candidature.created: {}", e.getMessage(), e);
            // L'erreur d'audit ne doit pas bloquer le traitement du message
            // donc on ne relance pas l'exception
        }
    }

    /**
     * Audit des événements "statut changé"
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void auditStatutChanged(@Payload String message) {
        try {
            log.info("[AUDIT] Événement candidature.statut.changed reçu: {}", message);
            // TODO: Persister en EventLog table
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit statut.changed: {}", e.getMessage(), e);
        }
    }

    /**
     * Audit des événements "entretien planifié"
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void auditEntretienPlanifie(@Payload String message) {
        try {
            log.info("[AUDIT] Événement entretien.planifie reçu: {}", message);
            // TODO: Persister en EventLog table
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit entretien.planifie: {}", e.getMessage(), e);
        }
    }

    /**
     * Audit des événements "décision recruteur"
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER)
    public void auditRecruiterDecision(@Payload String message) {
        try {
            log.info("[AUDIT] Événement recruteur (approved/rejected) reçu: {}", message);
            // TODO: Persister en EventLog table
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit recruteur decision: {}", e.getMessage(), e);
        }
    }

    /**
     * Audit des événements "authentification"
     * register, login, logout, etc.
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_AUTHENTIFICATION)
    public void auditAuthentication(@Payload String message) {
        try {
            log.info("[AUDIT] Événement authentification reçu: {}", message);
            // TODO: Persister en EventLog table
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit authentification: {}", e.getMessage(), e);
        }
    }

    /**
     * Audit des événements "actions admin"
     * user.blocked, user.deleted
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER)
    public void auditAdminAction(@Payload String message) {
        try {
            log.info("[AUDIT] Événement admin (user blocked/deleted) reçu: {}", message);
            // TODO: Persister en EventLog table
        } catch (Exception e) {
            log.error("[AUDIT ERROR] Erreur lors de l'audit admin action: {}", e.getMessage(), e);
        }
    }
}

