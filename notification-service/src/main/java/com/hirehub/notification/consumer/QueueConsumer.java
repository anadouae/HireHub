package com.hirehub.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.notification.services.MailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class QueueConsumer {

    private final MailService mailService;
    private final ObjectMapper objectMapper;

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
     *   "candidatName": "Jean Dupont",
     *   "offreId": 1
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
            // Parser le JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            String candidatEmail = jsonNode.get("candidatEmail").asText();
            String candidatName = jsonNode.get("candidatName").asText();
            String offreTitle = jsonNode.get("offreTitle").asText();
            Long offreId = jsonNode.get("offreId").asLong();

            // Envoyer l'email
            mailService.sendCandidatureConfirmation(candidatEmail, candidatName, offreTitle, offreId);

            log.info("[✅ CANDIDATURE] Email de confirmation envoyé à: {}", candidatEmail);
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE] Erreur lors du traitement: {}", e.getMessage(), e);
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
     *   "candidatName": "Jean Dupont",
     *   "offreTitle": "Développeur Java Senior",
     *   "ancienStatut": "EN_ATTENTE",
     *   "nouveauStatut": "ACCEPTÉ",
     *   "commentaire": "Bravo, vous êtes sélectionné!"
     * }
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_STATUT)
    public void handleStatutChanged(@Payload String message) {
        try {
            log.info("[STATUT CHANGÉ] Message reçu: {}", message);

            // Parser le JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            String candidatEmail = jsonNode.get("candidatEmail").asText();
            String candidatName = jsonNode.get("candidatName").asText();
            String offreTitle = jsonNode.get("offreTitle").asText();
            String ancienStatut = jsonNode.get("ancienStatut").asText();
            String nouveauStatut = jsonNode.get("nouveauStatut").asText();
            String commentaire = jsonNode.has("commentaire") ? jsonNode.get("commentaire").asText() : null;

            // Envoyer l'email de notification
            mailService.sendStatutChangedNotification(candidatEmail, candidatName, offreTitle,
                                                     ancienStatut, nouveauStatut, commentaire);

            log.info("[✅ STATUT] Email de notification envoyé à: {} | Nouveau statut: {}",
                    candidatEmail, nouveauStatut);
        } catch (Exception e) {
            log.error("[❌ STATUT] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement changement statut", e);
        }
    }

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 3: Entretien planifié                            ║
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
     *   "offreTitle": "Développeur Java Senior",
     *   "dateEntretien": "2026-04-15T10:30:00",
     *   "lieu": "Bureau Paris, 3e étage, Salle A",
     *   "interviewerName": "Marie Durand"
     * }
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
    public void handleEntretienPlanifie(@Payload String message) {
        try {
            log.info("[ENTRETIEN PLANIFIÉ] Message reçu: {}", message);

            // Parser le JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            String candidatEmail = jsonNode.get("candidatEmail").asText();
            String candidatName = jsonNode.get("candidatName").asText();
            String offreTitle = jsonNode.get("offreTitle").asText();
            String dateEntretien = jsonNode.get("dateEntretien").asText();
            String lieu = jsonNode.get("lieu").asText();
            String interviewerName = jsonNode.get("interviewerName").asText();

            // Envoyer l'email avec les détails de l'entretien
            mailService.sendEntretienPlanification(candidatEmail, candidatName, offreTitle,
                                                  dateEntretien, lieu, interviewerName);

            log.info("[✅ ENTRETIEN] Email de planification envoyé à: {} pour le: {}",
                    candidatEmail, dateEntretien);
        } catch (Exception e) {
            log.error("[❌ ENTRETIEN] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement entretien planifié", e);
        }
    }

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 4: Messages recruteur (routings legacy / notif)     ║
     * ╚────────────────────────────────────────────────────────────╝
     *
     * Événements:
     * - recruiter.request.approved / rejected (routings historiques)
     *
     * Source: auth-service ou autre publisher d'événements métier
     * Action: Envoyer email si ces routings sont encore utilisés
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

            // Parser le JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            String email = jsonNode.get("email").asText();
            String name = jsonNode.get("name").asText();
            String status = jsonNode.get("status").asText();
            String statusMessage = jsonNode.has("message") ? jsonNode.get("message").asText() : "";

            // Construire le sujet et le corps selon le statut
            String subject = "Décision concernant votre inscription - HireHub";
            String body = buildRecruiterDecisionEmail(name, status, statusMessage);

            // Envoyer l'email
            mailService.sendHtmlEmail(email, subject, body);

            log.info("[✅ RECRUTEUR] Email de décision envoyé à: {}", email);
        } catch (Exception e) {
            log.error("[❌ RECRUTEUR] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement décision recruteur", e);
        }
    }

    /**
     * ╔────────────────────────────────────────────────────────────╗
     * ║  LISTENER 5: Actions administrateur sur utilisateurs        ║
     * ╚────────────────────────────────────────────────────────────╝
     *
     * Événements:
     * - admin.user.created / deleted / suspended
     *
     * Source: auth-service ou admin-service
     * Action: Audit ou notification administrative
     */
    @RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ADMIN_USER)
    public void handleAdminUserAction(@Payload String message) {
        try {
            log.info("[ADMIN USER ACTION] Message reçu: {}", message);

            // Parser le JSON pour récupérer les informations d'audit
            JsonNode jsonNode = objectMapper.readTree(message);
            String action = jsonNode.get("action").asText();
            String userEmail = jsonNode.get("userEmail").asText();
            String userName = jsonNode.get("userName").asText();
            String adminEmail = jsonNode.has("adminEmail") ? jsonNode.get("adminEmail").asText() : "admin@hirehub.com";

            log.info("[AUDIT] Action: {} | Utilisateur: {} | Par: {}", action, userEmail, adminEmail);
            log.info("[✅ ADMIN ACTION] Audit/notification traitée");
        } catch (Exception e) {
            log.error("[❌ ADMIN ACTION] Erreur lors du traitement: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement action admin utilisateur", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER METHODS - Construction des templates email
    // ═══════════════════════════════════════════════════════════════

    private String buildRecruiterDecisionEmail(String name, String status, String statusMessage) {
        String statusColor = status.equalsIgnoreCase("APPROVED") ? "#10b981" : "#ef4444";
        String statusTitle = status.equalsIgnoreCase("APPROVED") ? "Inscription approuvée ✅" : "Inscription rejetée ❌";

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                    .header { background-color: %s; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 20px; }
                    .footer { background-color: #f9fafb; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>%s</p>
                        <p>Cordialement,<br>L'équipe HireHub</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 HireHub. Tous droits réservés.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(statusColor, statusTitle, name, statusMessage);
    }
}


