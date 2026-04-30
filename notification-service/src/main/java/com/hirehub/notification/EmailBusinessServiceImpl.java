package com.hirehub.notification;

import com.hirehub.notification.email.interfaces.EmailBusinessService;
import com.hirehub.notification.template.EmailTemplateForAuthentification;
import com.hirehub.common.dtos.candidatures.CandidatureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.hirehub.notification.template.EmailTemplateForCandidature.*;

@Slf4j
@Service
public class EmailBusinessServiceImpl extends MailService implements EmailBusinessService {

    public EmailBusinessServiceImpl(JavaMailSender mailSender) {

        super(mailSender);
    }

    /**
     * <pre>
     * Template: Email de confirmation de candidature
     * @param candidatureDTO DTO contenant les informations nécessaires pour construire l'email
     * { "candidatEmail", // Email du candidat
     * "candidatName", // Nom du candidat
     * "offreTitle", // Titre de l'offre d'emploi
     * "offreId", // ID de l'offree
     * }
     * </pre>
     */
    public void sendCandidatureConfirmation(CandidatureDTO candidatureDTO) {

        try {
            String subject = "Votre candidature a été reçue - HireHub";

            String htmlBody = buildCandidatureConfirmationTemplate(
                    candidatureDTO.getCandidatName(), candidatureDTO.getOffreTitle());

            sendHtmlEmail(candidatureDTO.getCandidatEmail(), subject, htmlBody);
            log.info("[📧 CANDIDATURE] Confirmation envoyée à: {} pour l'offre: {}",
                    candidatureDTO.getCandidatEmail(), candidatureDTO.getOffreTitle());
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE] Erreur lors de l'envoi de confirmation à {}: {}",
                    candidatureDTO.getCandidatEmail(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de confirmation de candidature", e);
        }
    }

    /**
     * Template : Email de changement de statut de candidature
     *
     * @param candidatEmail Email du candidat
     * @param candidatName  Nom du candidat
     * @param offreTitle    Titre de l'offre d'emploi
     * @param ancienStatut  Ancien statut
     * @param nouveauStatut Nouveau statut
     * @param commentaire   Commentaire optionnel (peut être null)
     */
    public void sendCandidatureStatutChangedNotification(String candidatEmail, String candidatName,
                                                         String offreTitle, String ancienStatut,
                                                         String nouveauStatut, String commentaire) {
        try {
            String subject = "Mise à jour de votre candidature - HireHub";

            String htmlBody = buildCandidatureStatutChangedTemplate(candidatName, offreTitle,
                    nouveauStatut, commentaire);

            sendHtmlEmail(candidatEmail, subject, htmlBody);
            log.info("[📧 STATUT] Notification envoyée à: {} | Nouveau statut: {}",
                    candidatEmail, nouveauStatut);
        } catch (Exception e) {
            log.error("[❌ STATUT] Erreur lors de l'envoi de event à {}: {}",
                    candidatEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de event de changement de statut", e);
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

            String htmlBody = buildCandidatureEntretienTemplate(candidatName, offreTitle,
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

            String htmlBody = buildCandidatureEntretienAnnulationTemplate(candidatName, offreTitle, raison);

            sendHtmlEmail(candidatEmail, subject, htmlBody);
            log.info("[📧 ANNULATION] Notification envoyée à: {}", candidatEmail);
        } catch (Exception e) {
            log.error("[❌ ANNULATION] Erreur lors de l'envoi d'annulation à {}: {}",
                    candidatEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi d'annulation d'entretien", e);
        }
    }

    @Override
    public void sendRegisterOtp(String email, String userName, String otpCode, int otpValidityMinutes) {
        try {
            String subject = "Code OTP de verification - HireHub";
            String html = EmailTemplateForAuthentification.buildRegisterOtpTemplate(userName, otpCode, otpValidityMinutes);
            sendHtmlEmail(email, subject, html);
            log.info("[📧 OTP] Code OTP envoyé à: {} | Validité: {} minutes", email, otpValidityMinutes);
        } catch (Exception e) {
            log.error("[❌ OTP] Erreur lors de l'envoi de l'OTP à {}: {}",
                    email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi du code OTP", e);
        }
    }

    @Override
    public void sendLoginAlert(String email, String userName, String loginDateTime, String ipAddress, String userAgent) {
        try{
            String subject = "Alerte connexion - HireHub";
            String html = EmailTemplateForAuthentification.buildLoginAlertTemplate(userName, loginDateTime, ipAddress, userAgent);
            sendHtmlEmail(email, subject, html);
            log.info("[📧 LOGIN ALERT] Alerte de connexion envoyée à: {} | Date/Heure: {}", email, loginDateTime);
        } catch (Exception e) {
            log.error("[❌ LOGIN ALERT] Erreur lors de l'envoi de l'alerte de connexion à {}: {}",
                    email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'alerte de connexion", e);
        }
    }

    @Override
    public void sendLogoutInfo(String email, String userName, String logoutDateTime) {
        try {
            String subject = "Confirmation deconnexion - HireHub";
            String html = EmailTemplateForAuthentification.buildLogoutInfoTemplate(userName, logoutDateTime);
            sendHtmlEmail(email, subject, html);
            log.info("[📧 LOGOUT INFO] Confirmation de déconnexion envoyée à: {} | Date/Heure: {}",
                    email, logoutDateTime);
        }catch (Exception e) {
            log.error("[❌ LOGOUT INFO] Erreur lors de l'envoi de la confirmation de déconnexion à {}: {}",
                    email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de la confirmation de déconnexion", e);
        }
    }

    /**
     * Template: Email de création/confirmation de candidature
     *
     * @param email Email du candidat
     * @param offerTitle Titre de l'offre
     */
    public void sendCandidatureCreatedEmail(String email, String offerTitle) {
        try {
            String subject = "Candidature reçue - HireHub";
            String htmlBody = buildCandidatureConfirmationTemplate("Candidat", offerTitle);
            sendHtmlEmail(email, subject, htmlBody);
            log.info("[📧 CANDIDATURE CREATED] Email envoyé à: {}", email);
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE CREATED] Erreur lors de l'envoi à {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de la création de candidature", e);
        }
    }
}

