package com.hirehub.notification.services;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║              Email Service Interface - HireHub                ║
 * ║                                                                ║
 * ║  Contrat pour tous les services d'envoi d'emails             ║
 * ║  Permet l'utilisation de différents fournisseurs             ║
 * ║  (Gmail, SendGrid, etc.)                                      ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public interface EmailService {

    /**
     * Envoie un email simple
     * @param to Email du destinataire
     * @param subject Sujet
     * @param body Corps du message
     */
    void sendSimpleEmail(String to, String subject, String body);

    /**
     * Envoie un email HTML
     * @param to Email du destinataire
     * @param subject Sujet
     * @param htmlBody Corps du message en HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Envoie un email HTML à plusieurs destinataires
     * @param to Tableau d'emails
     * @param subject Sujet
     * @param htmlBody Corps du message en HTML
     */
    void sendHtmlEmailMultiple(String[] to, String subject, String htmlBody);

    /**
     * Template: Confirmation de candidature
     */
    void sendCandidatureConfirmation(String candidatEmail, String candidatName,
                                     String offreTitle, Long offreId);

    /**
     * Template: Changement de statut
     */
    void sendStatutChangedNotification(String candidatEmail, String candidatName,
                                      String offreTitle, String ancienStatut,
                                      String nouveauStatut, String commentaire);

    /**
     * Template: Planification d'entretien
     */
    void sendEntretienPlanification(String candidatEmail, String candidatName,
                                   String offreTitle, String dateEntretien,
                                   String lieux, String interviewer);

    /**
     * Template: Annulation d'entretien
     */
    void sendEntretienAnnulation(String candidatEmail, String candidatName,
                                String offreTitle, String raison);
}