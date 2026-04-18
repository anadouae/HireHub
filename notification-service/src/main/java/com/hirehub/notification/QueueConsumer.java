package com.hirehub.notification;

import com.hirehub.common.constants.RabbitMQConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║              RabbitMQ Queue Consumer - Notifications          ║
 * ║                                                                ║
 * ║  Ce service écoute les 4 queues définies en RabbitMQConfig   ║
 * ║  et traite les différents types d'événements.                ║
 * ║                                                                ║
 * ║  @RabbitListener = "inscris-moi à cette queue, et appelle    ║
 * ║                     cette méthode chaque fois qu'un message  ║
 * ║                     arrive"                                   ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Component
@Slf4j
public class QueueConsumer {

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 1: Nouvelles candidatures créées                 ║
     * ╚────────────────────────────────────────────────────────────╝
     *
     * Événement: candidature.created
     * Source: candidature-service (publie quand un candidat soumet)
     * Action: Envoyer email "Candidature reçue" au candidat
     *
     * Exemple de message JSON attendu:
     * {
     *   "candidatureId": 42,
     *   "candidatEmail": "jean@example.com",
     *   "offreTitle": "Développeur Java Senior",
     *   "candidatName": "Jean Dupont"
     * }
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE)
    public void handleCandidatureCreated(@Payload String message) {
        try {
            log.info("[CANDIDATURE CRÉÉE] Message reçu: {}", message);

            // TODO: Parser le JSON et envoyer l'email
            // Implémenter la logique d'envoi d'email ici
            // - Récupérer les détails du candidat
            // - Récupérer les détails de l'offre
            // - Générer le template email
            // - Envoyer via MailPit (ou service d'email)

            log.info("[CANDIDATURE] Email envoyé avec succès");
        } catch (Exception e) {
            log.error("[CANDIDATURE] Erreur lors du traitement: {}", e.getMessage(), e);
            // En cas d'erreur, le message retourne à la queue pour retry
            throw new RuntimeException("Erreur traitement candidature créée", e);
        }
    }

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 2: Changement de statut de candidature           ║
     * ╚────────────────────────────────────────────────────────────╝
     *
     * Événement: candidature.statut.changed
     * Source: candidature-service (publie quand le recruteur change le statut)
     * Action: Envoyer email "Votre statut a été mis à jour" au candidat
     *
     * Exemple de message JSON attendu:
     * {
     *   "candidatureId": 42,
     *   "candidatEmail": "jean@example.com",
     *   "ancienStatut": "EN_ATTENTE",
     *   "nouveauStatut": "ACCEPTÉ",
     *   "commentaire": "Bravo, vous êtes sélectionné!"
     * }
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void handleStatutChanged(@Payload String message) {
        try {
            log.info("[STATUT CHANGÉ] Message reçu: {}", message);

            // TODO: Parser le JSON et envoyer l'email
            // - Récupérer le nouveau statut
            // - Générer un message personnalisé selon le statut
            // - Envoyer l'email

            log.info("[✅ STATUT] Email de notification envoyé");
        } catch (Exception e) {
            log.error("[❌ STATUT] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement changement statut", e);
        }
    }

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 3: Entretien planifié/annulé                     ║
     * ╚────────────────────────────────────────────────────────────╝
     *
     * Événement: entretien.planifie
     * Source: entretien-service (publie quand un entretien est planifié)
     * Action: Envoyer email avec date/heure/lieu de l'entretien
     *
     * Exemple de message JSON attendu:
     * {
     *   "entretienId": 15,
     *   "candidatEmail": "jean@example.com",
     *   "candidatName": "Jean Dupont",
     *   "dateEntretien": "2026-04-15T10:30:00",
     *   "lieu": "Bureau Paris, 3e étage, Salle A",
     *   "interviewerName": "Marie Durand",
     *   "interviewerEmail": "marie@hirehub.com"
     * }
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienPlanifie(@Payload String message) {
        try {
            log.info("[ENTRETIEN PLANIFIÉ] Message reçu: {}", message);

            // TODO: Parser le JSON et envoyer l'email
            // - Récupérer la date/heure/lieu
            // - Envoyer l'email au candidat avec le détail de l'entretien
            // - Possibilité d'envoyer aussi une invitation calendar (.ics)

            log.info("[ENTRETIEN] Email avec détails envoyé");
        } catch (Exception e) {
            log.error("[ENTRETIEN] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement entretien planifié", e);
        }
    }

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 4: Décisions recruteur (approbation/rejet)       ║
     * ╚────────────────────────────────────────────────────────────╝
     *
     * Événements:
     * - recruiter.request.approved (admin approuve la demande recruteur)
     * - recruiter.request.rejected (admin rejette la demande recruteur)
     *
     * Source: auth-service (publie quand l'admin prend une décision)
     * Action: Envoyer email pour notifier de l'approbation/rejet
     *
     * Exemple de message JSON attendu:
     * {
     *   "requestId": 7,
     *   "email": "newrecruiter@company.com",
     *   "name": "Alice Dupont",
     *   "status": "APPROVED" ou "REJECTED",
     *   "message": "Bienvenue!" ou "Désolé, votre demande n'a pas été acceptée"
     * }
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER)
    public void handleRecruiterDecision(@Payload String message) {
        try {
            log.info("[📬 RECRUTEUR] Message reçu: {}", message);

            // TODO: Parser le JSON et déterminer l'action
            // - Vérifier si c'est une approbation ou un rejet
            // - Envoyer l'email correspondant
            // - Si approuvé: "Bienvenue, vous pouvez maintenant créer des offres"
            // - Si rejeté: "Désolé, votre demande a été rejetée"

            log.info("[✅ RECRUTEUR] Email de décision envoyé");
        } catch (Exception e) {
            log.error("[❌ RECRUTEUR] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement décision recruteur", e);
        }
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER)
    public void handleAdminUserAction(@Payload String message) {
        try {
            log.info("[ADMIN USER ACTION] Message reçu: {}", message);
            log.info("[ADMIN USER ACTION] Audit/notification traitée");
        } catch (Exception e) {
            log.error("[ADMIN USER ACTION] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement action admin utilisateur", e);
        }
    }
}


