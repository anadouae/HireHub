package com.hirehub.email;

import com.hirehub.common.dtos.candidatures.CandidatureRabbitDTO;
import com.hirehub.email.email.interfaces.EmailBusinessService;
import com.hirehub.email.template.EmailTemplateForAdminRecruiter;
import com.hirehub.email.template.EmailTemplateForAuthentification;
import com.hirehub.email.dto.CandidateInfoDTO;
import com.hirehub.email.feign.CandidateServiceClientAPI;
import com.hirehub.common.dtos.candidatures.CandidatureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.hirehub.email.template.EmailTemplateForCandidature.*;

@Slf4j
@Service
public class EmailBusinessServiceImpl extends MailService implements EmailBusinessService {

    @Autowired
    private final CandidateServiceClientAPI candidateServiceClientAPI;

    public EmailBusinessServiceImpl(JavaMailSender mailSender,
                                    CandidateServiceClientAPI candidateServiceClientAPI) {

        super(mailSender);
        this.candidateServiceClientAPI = candidateServiceClientAPI;
    }

    private CandidateInfoDTO resolveCandidateInfo(String candidateId) {
        if (candidateId == null || candidateId.isBlank()) {
            return null;
        }

        try {
            return candidateServiceClientAPI.getCandidateById(candidateId);
        } catch (Exception e) {
            log.warn("[CANDIDATE LOOKUP] Impossible de récupérer le candidat {}: {}", candidateId, e.getMessage());
            return null;
        }
    }

    private String resolveCandidateName(CandidateInfoDTO candidate) {
        if (candidate == null) {
            return "Candidat";
        }
        if (candidate.fullName != null && !candidate.fullName.isBlank()) {
            return candidate.fullName;
        }
        String firstName = candidate.firstName == null ? "" : candidate.firstName.trim();
        String lastName = candidate.lastName == null ? "" : candidate.lastName.trim();
        String resolved = (firstName + " " + lastName).trim();
        return resolved.isBlank() ? "Candidat" : resolved;
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
    public void sendCandidatureConfirmation(CandidatureRabbitDTO candidatureDTO) {

        try {
            CandidateInfoDTO candidateInfo = resolveCandidateInfo(candidatureDTO.getCandidatId());
            String candidateEmail = candidateInfo != null && candidateInfo.email != null && !candidateInfo.email.isBlank()
                    ? candidateInfo.email
                    : null;
            String candidateName = resolveCandidateName(candidateInfo);

            String subject = "Votre candidature a été reçue - HireHub";

            String htmlBody = buildCandidatureConfirmationTemplate(
                    candidateName, "Offre");

            if (candidateEmail == null || candidateEmail.isBlank()) {
                throw new IllegalStateException("Email candidat introuvable pour l'id: " + candidatureDTO.getCandidatId());
            }

            sendHtmlEmail(candidateEmail, subject, htmlBody);
            log.info("[📧 CANDIDATURE] Confirmation envoyée à: {}", candidateEmail);
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE] Erreur lors de l'envoi de confirmation pour candidat {}: {}",
                    candidatureDTO.getCandidatId(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de confirmation de candidature", e);
        }
    }

    /**
     * Template: Confirmation de candidature
     *
     * @param candidatureDTO
     */
    @Override
    public void sendCandidatureConfirmation(CandidatureDTO candidatureDTO) {
        try {
            CandidateInfoDTO candidateInfo =
                    resolveCandidateInfo(candidatureDTO.getCandidatId());
            String candidateEmail = candidateInfo != null && candidateInfo.email != null && !candidateInfo.email.isBlank()
                    ? candidateInfo.email
                    : null;
            String candidateName = resolveCandidateName(candidateInfo);

            String subject = "Votre candidature a été reçue - HireHub";

            String htmlBody = buildCandidatureConfirmationTemplate(
                    candidateName, "Offre");

            if (candidateEmail == null || candidateEmail.isBlank()) {
                throw new IllegalStateException("Email candidat introuvable pour l'id: " + candidatureDTO.getCandidatId());
            }

            sendHtmlEmail(candidateEmail, subject, htmlBody);
            log.info("[📧 CANDIDATURE] Confirmation envoyée à: {}", candidateEmail);
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE] Erreur lors de l'envoi de confirmation pour candidat {}: {}",
                    candidatureDTO.getCandidatId(), e.getMessage(), e);
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

    public void sendRegisterWelcome(String email, String userName) {
        try {
            String html = EmailTemplateForAuthentification.buildRegisterWelcomeTemplate(
                    userName != null ? userName : email);
            sendHtmlEmail(email, "Bienvenue sur HireHub", html);
            log.info("[📧 REGISTER] Email de bienvenue envoyé à {}", email);
        } catch (Exception e) {
            log.error("[❌ REGISTER] Erreur envoi bienvenue à {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi email bienvenue", e);
        }
    }

    /**
     * Template: Email de création/confirmation de candidature
     *
     * @param email Email du candidat
     * @param offerTitle Titre de l'offre
     */
    public void sendCandidatureCreatedEmail(String email, String offerTitle) {
        sendCandidatureCreatedEmail(email, "Candidat", offerTitle);
    }

    public void sendCandidatureCreatedEmail(String email, String candidateName, String offerTitle) {
        try {
            String subject = "Candidature reçue - HireHub";
            String title = offerTitle != null && !offerTitle.isBlank() ? offerTitle : "Offre";
            String name = candidateName != null && !candidateName.isBlank() ? candidateName : "Candidat";
            String htmlBody = buildCandidatureConfirmationTemplate(name, title);
            sendHtmlEmail(email, subject, htmlBody);
            log.info("[📧 CANDIDATURE CREATED] Email envoyé à: {}", email);
        } catch (Exception e) {
            log.error("[❌ CANDIDATURE CREATED] Erreur lors de l'envoi à {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de la création de candidature", e);
        }
    }

    public void sendAdminUserAction(String email, String userName, String action, String role) {
        try {
            String subject = switch (action) {
                case "BLOCKED" -> "Compte suspendu - HireHub";
                case "UNBLOCKED" -> "Compte réactivé - HireHub";
                case "DELETED" -> "Compte supprimé - HireHub";
                default -> "Action sur votre compte - HireHub";
            };
            String html = EmailTemplateForAdminRecruiter.buildAdminUserActionTemplate(
                    userName != null ? userName : email,
                    action,
                    role != null ? role : ""
            );
            sendHtmlEmail(email, subject, html);
            log.info("[📧 ADMIN] Notification {} envoyée à {}", action, email);
        } catch (Exception e) {
            log.error("[❌ ADMIN] Erreur envoi à {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi notification admin", e);
        }
    }

    public void sendRecruiterApproved(String email, String userName) {
        try {
            String html = EmailTemplateForAdminRecruiter.buildRecruiterApprovedTemplate(
                    userName != null ? userName : email);
            sendHtmlEmail(email, "Inscription recruteur approuvée - HireHub", html);
            log.info("[📧 RECRUITER] Approbation envoyée à {}", email);
        } catch (Exception e) {
            log.error("[❌ RECRUITER] Erreur approbation {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi approbation recruteur", e);
        }
    }

    public void sendRecruiterRejected(String email, String userName, String reason) {
        try {
            String html = EmailTemplateForAdminRecruiter.buildRecruiterRejectedTemplate(
                    userName != null ? userName : email, reason);
            sendHtmlEmail(email, "Inscription recruteur refusée - HireHub", html);
            log.info("[📧 RECRUITER] Refus envoyé à {}", email);
        } catch (Exception e) {
            log.error("[❌ RECRUITER] Erreur refus {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi refus recruteur", e);
        }
    }

    public void sendRecruiterNewCandidature(String email, String recruiterName, String offerTitle) {
        try {
            String title = offerTitle != null && !offerTitle.isBlank() ? offerTitle : "Offre";
            String name = recruiterName != null && !recruiterName.isBlank() ? recruiterName : "Recruteur";
            String html = buildRecruiterNewCandidatureTemplate(name, title);
            sendHtmlEmail(email, "Nouvelle candidature sur votre offre - HireHub", html);
            log.info("[📧 RECRUITER NEW] Notification envoyée à {}", email);
        } catch (Exception e) {
            log.error("[❌ RECRUITER NEW] Erreur {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi notification nouvelle candidature", e);
        }
    }

    public void sendOfferClosedSummary(String email, String recruiterName, String offerTitle, int rejectedCount) {
        try {
            String title = offerTitle != null && !offerTitle.isBlank() ? offerTitle : "Offre";
            String name = recruiterName != null && !recruiterName.isBlank() ? recruiterName : "Recruteur";
            String html = buildOfferClosedSummaryTemplate(name, title, rejectedCount);
            sendHtmlEmail(email, "Offre fermée - récapitulatif HireHub", html);
            log.info("[📧 OFFER CLOSED] Récap envoyé à {} ({} refus auto)", email, rejectedCount);
        } catch (Exception e) {
            log.error("[❌ OFFER CLOSED] Erreur {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi récap fermeture offre", e);
        }
    }

    public void sendRecruiterReviewRequired(String email, String userName, String message) {
        try {
            String html = EmailTemplateForAdminRecruiter.buildRecruiterReviewRequiredTemplate(
                    userName != null ? userName : email, message);
            sendHtmlEmail(email, "Vérification recruteur en cours - HireHub", html);
            log.info("[📧 RECRUITER] Revue manuelle notifiée à {}", email);
        } catch (Exception e) {
            log.error("[❌ RECRUITER] Erreur revue {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erreur envoi revue recruteur", e);
        }
    }
}

