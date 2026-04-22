package com.hirehub.notification.services.email;

import com.hirehub.notification.services.email.interfaces.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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


}

