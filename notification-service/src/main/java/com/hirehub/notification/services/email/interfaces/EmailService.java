package com.hirehub.notification.services.email.interfaces;

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

}