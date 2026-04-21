package com.hirehub.notification.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static com.hirehub.notification.services.EmailTemplate.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                   Mail Service - HireHub                      ║
 * ║                                                                ║
 * ║  Service centralisé pour l'envoi d'emails avec templates     ║
 * ║  Gère tous les types de notifications du système.             ║
 * ║                                                                ║
 * ║  Support pour:                                                ║
 * ║  - Emails simples (texte uniquement)                          ║
 * ║  - Emails HTML (avec mise en forme)                           ║
 * ║  - Gestion des erreurs et logging complet                     ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Service
@Slf4j
public class MailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("[MAIL SERVICE] Initialized with email: {}", fromEmail);
    }

    /**
     * Envoie un email simple (texte uniquement)
     *
     * @param to          Email du destinataire
     * @param subject     Sujet de l'email
     * @param body        Corps du message (texte simple)
     * @throws RuntimeException en cas d'erreur d'envoi
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            log.info("[MAIL] Envoi email simple à: {} | Sujet: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("[✅ MAIL SIMPLE] Email envoyé avec succès à: {}", to);
        } catch (Exception e) {
            log.error("[❌ MAIL SIMPLE] Erreur lors de l'envoi à {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email simple", e);
        }
    }

    /**
     * Envoie un email HTML (avec mise en forme)
     *
     * @param to          Email du destinataire
     * @param subject     Sujet de l'email
     * @param htmlBody    Corps du message (HTML)
     * @throws RuntimeException en cas d'erreur d'envoi
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            log.info("[MAIL] Envoi email HTML à: {} | Sujet: {}", to, subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);

            log.info("[✅ MAIL HTML] Email envoyé avec succès à: {}", to);
        } catch (MessagingException e) {
            log.error("[❌ MAIL HTML] Erreur lors de l'envoi à {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email HTML", e);
        }
    }

    /**
     * Envoie un email HTML avec destinataires multiples
     *
     * @param to          Tableau des emails destinataires
     * @param subject     Sujet de l'email
     * @param htmlBody    Corps du message (HTML)
     * @throws RuntimeException en cas d'erreur d'envoi
     */
    public void sendHtmlEmailMultiple(String[] to, String subject, String htmlBody) {
        try {
            log.info("[MAIL] Envoi email HTML à: {} destinataires | Sujet: {}", to.length, subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);

            log.info("[✅ MAIL HTML MULTIPLE] Email envoyé avec succès à {} destinataire(s)", to.length);
        } catch (MessagingException e) {
            log.error("[❌ MAIL HTML MULTIPLE] Erreur lors de l'envoi à {} destinataires: {}",
                    to.length, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email HTML à plusieurs destinataires", e);
        }
    }

    /**
     * Template: Email de confirmation de candidature
     *
     * @param candidatEmail Email du candidat
     * @param candidatName  Nom du candidat
     * @param offreTitle    Titre de l'offre d'emploi
     * @param offreId       ID de l'offre
     */
    public void sendCandidatureConfirmation(String candidatEmail, String candidatName,
                                            String offreTitle, Long offreId) {
        try {
            String subject = "Votre candidature a été reçue - HireHub";

            String htmlBody = buildCandidatureConfirmationTemplate(candidatName, offreTitle);

            sendHtmlEmail(candidatEmail, subject, htmlBody);
            log.info("[📧 CANDIDATURE] Confirmation envoyée à: {} pour l'offre: {}",
                    candidatEmail, offreTitle);
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE] Erreur lors de l'envoi de confirmation à {}: {}",
                    candidatEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de confirmation de candidature", e);
        }
    }

    /**
     * Template: Email de changement de statut de candidature
     *
     * @param candidatEmail Email du candidat
     * @param candidatName  Nom du candidat
     * @param offreTitle    Titre de l'offre d'emploi
     * @param ancienStatut  Ancien statut
     * @param nouveauStatut Nouveau statut
     * @param commentaire   Commentaire optionnel (peut être null)
     */
    public void sendStatutChangedNotification(String candidatEmail, String candidatName,
                                             String offreTitle, String ancienStatut,
                                             String nouveauStatut, String commentaire) {
        try {
            String subject = "Mise à jour de votre candidature - HireHub";

            String htmlBody = buildStatutChangedTemplate(candidatName, offreTitle,
                                                         nouveauStatut, commentaire);

            sendHtmlEmail(candidatEmail, subject, htmlBody);
            log.info("[📧 STATUT] Notification envoyée à: {} | Nouveau statut: {}",
                    candidatEmail, nouveauStatut);
        } catch (Exception e) {
            log.error("[❌ STATUT] Erreur lors de l'envoi de notification à {}: {}",
                    candidatEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de notification de changement de statut", e);
        }
    }

    /**
     * Template: Email de planification d'entretien
     *
     * @param candidatEmail Email du candidat
     * @param candidatName  Nom du candidat
     * @param offreTitle    Titre de l'offre d'emploi
     * @param dateEntretien Date et heure de l'entretien
     * @param lieux         Lieu de l'entretien (en ligne ou adresse)
     * @param interviewer   Nom du recruteur/interviewer
     */
    public void sendEntretienPlanification(String candidatEmail, String candidatName,
                                          String offreTitle, String dateEntretien,
                                          String lieux, String interviewer) {
        try {
            String subject = "Entretien programmé - HireHub";

            String htmlBody = buildEntretienTemplate(candidatName, offreTitle,
                                                     dateEntretien, lieux, interviewer);

            sendHtmlEmail(candidatEmail, subject, htmlBody);
            log.info("[📧 ENTRETIEN] Planification envoyée à: {} pour le: {}",
                    candidatEmail, dateEntretien);
        } catch (Exception e) {
            log.error("[❌ ENTRETIEN] Erreur lors de l'envoi de planification à {}: {}",
                    candidatEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de planification d'entretien", e);
        }
    }

    /**
     * Template: Email d'annulation d'entretien
     *
     * @param candidatEmail Email du candidat
     * @param candidatName  Nom du candidat
     * @param offreTitle    Titre de l'offre d'emploi
     * @param raison        Raison de l'annulation (optionnelle)
     */
    public void sendEntretienAnnulation(String candidatEmail, String candidatName,
                                       String offreTitle, String raison) {
        try {
            String subject = "Annulation d'entretien - HireHub";

            String htmlBody = buildEntretienAnnulationTemplate(candidatName, offreTitle, raison);

            sendHtmlEmail(candidatEmail, subject, htmlBody);
            log.info("[📧 ANNULATION] Notification envoyée à: {}", candidatEmail);
        } catch (Exception e) {
            log.error("[❌ ANNULATION] Erreur lors de l'envoi d'annulation à {}: {}",
                    candidatEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi d'annulation d'entretien", e);
        }
    }

}

