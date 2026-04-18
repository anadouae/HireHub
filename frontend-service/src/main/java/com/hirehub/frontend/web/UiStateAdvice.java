package com.hirehub.frontend.web;

import com.hirehub.common.enums.RecruiterVerificationStatus;
import com.hirehub.common.enums.UserRole;
import com.hirehub.frontend.auth.HirehubUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * État UI basé sur la session Spring Security (pas de ?demo=).
 * Un seul argument Model : lecture de l'authentification via SecurityContextHolder
 * (évite les erreurs de binding Spring si le bytecode est compilé sans -parameters).
 */
@ControllerAdvice(basePackages = "com.hirehub.frontend.web")
public class UiStateAdvice {

    @Value("${hirehub.api-base-url}")
    private String apiBaseUrl;
    @Value("${hirehub.recaptcha.site-key:}")
    private String recaptchaSiteKey;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleOAuthClientId;

    @ModelAttribute
    public void uiDefaults(Model model) {
        model.addAttribute("apiBaseUrl", apiBaseUrl);
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        model.addAttribute("googleSignInEnabled", StringUtils.hasText(googleOAuthClientId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            model.addAttribute("uiConnecte", false);
            model.addAttribute("uiEstAdmin", false);
            model.addAttribute("uiTypeCompte", "VISITEUR");
            model.addAttribute("uiMenuCandidat", false);
            model.addAttribute("uiMenuRecruteur", false);
            model.addAttribute("uiRecruteurPending", false);
            model.addAttribute("uiRecruteurApprouve", false);
            model.addAttribute("uiRecruteurAutoCheck", false);
            model.addAttribute("uiRecruteurReviewRequired", false);
            return;
        }

        Object currentPrincipal = auth.getPrincipal();
        if (!(currentPrincipal instanceof HirehubUserDetails)) {
            model.addAttribute("uiConnecte", true);
            model.addAttribute("uiEstAdmin", false);
            model.addAttribute("uiTypeCompte", "INCONNU");
            model.addAttribute("uiMenuCandidat", false);
            model.addAttribute("uiMenuRecruteur", false);
            model.addAttribute("uiRecruteurPending", false);
            model.addAttribute("uiRecruteurApprouve", false);
            model.addAttribute("uiRecruteurAutoCheck", false);
            model.addAttribute("uiRecruteurReviewRequired", false);
            return;
        }

        HirehubUserDetails details = (HirehubUserDetails) currentPrincipal;
        UserRole role = details.getRole();
        boolean admin = role == UserRole.ADMIN;
        boolean candidat = role == UserRole.CANDIDAT;
        boolean recruteur = role == UserRole.RECRUTEUR;

        boolean recruteurPending = false;
        boolean recruteurApprouve = recruteur;

        RecruiterVerificationStatus verificationStatus = null;
        if (details.getVerificationStatus() != null) {
            try {
                verificationStatus = RecruiterVerificationStatus.valueOf(details.getVerificationStatus());
            } catch (IllegalArgumentException ex) {
                verificationStatus = null;
            }
        }

        boolean autoCheck = recruteur && verificationStatus == RecruiterVerificationStatus.PENDING_AUTO_CHECK;
        boolean reviewRequired = recruteur && verificationStatus == RecruiterVerificationStatus.REVIEW_REQUIRED;

        model.addAttribute("uiConnecte", true);
        model.addAttribute("uiEstAdmin", admin);
        model.addAttribute("uiTypeCompte", role.name());
        model.addAttribute("uiMenuCandidat", candidat || admin);
        model.addAttribute("uiMenuRecruteur", recruteur);
        model.addAttribute("uiRecruteurPending", recruteurPending);
        model.addAttribute("uiRecruteurApprouve", recruteurApprouve);
        model.addAttribute("uiRecruteurAutoCheck", autoCheck);
        model.addAttribute("uiRecruteurReviewRequired", reviewRequired);
    }
}
