package com.hirehub.frontend.web;

import com.hirehub.frontend.signup.RegistrationSessionKeys;
import com.hirehub.frontend.signup.SignupEmailVerificationService;
import com.hirehub.frontend.signup.SignupSessionVerified;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class RegistrationVerificationController {

    private static final String ROLE_CANDIDAT = "CANDIDAT";
    private static final String ROLE_RECRUTEUR = "RECRUTEUR";

    private final SignupEmailVerificationService signupEmailVerificationService;

    public RegistrationVerificationController(SignupEmailVerificationService signupEmailVerificationService) {
        this.signupEmailVerificationService = signupEmailVerificationService;
    }

    /* ---------- Candidat ---------- */

    @GetMapping("/register/candidat")
    public String registerCandidatFinish(HttpSession session, Model model) {
        SignupSessionVerified v = verifiedOrNull(session, ROLE_CANDIDAT);
        if (v == null) {
            return "redirect:/register/candidat/email";
        }
        model.addAttribute("signupNom", v.getNomComplet());
        model.addAttribute("signupEmail", v.getEmail());
        return "pages/public/register-candidat";
    }

    @GetMapping("/register/candidat/email")
    public String registerCandidatEmail(HttpSession session) {
        session.removeAttribute(RegistrationSessionKeys.VERIFIED);
        return "pages/public/register-candidat-step-email";
    }

    @PostMapping("/register/candidat/email")
    public String registerCandidatEmailSubmit(
            @RequestParam("nomComplet") String nom,
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes ra
    ) {
        String result = signupEmailVerificationService.requestCode(email, nom, ROLE_CANDIDAT);
        if ("email_taken".equals(result)) {
            ra.addFlashAttribute("signupEmailError", "taken");
            return "redirect:/register/candidat/email";
        }
        if ("invalid".equals(result)) {
            ra.addFlashAttribute("signupEmailError", "invalid");
            return "redirect:/register/candidat/email";
        }
        session.setAttribute(RegistrationSessionKeys.PENDING_EMAIL, email.trim().toLowerCase());
        ra.addFlashAttribute("signupCodeSent", true);
        return "redirect:/register/candidat/code";
    }

    @GetMapping("/register/candidat/code")
    public String registerCandidatCode(HttpSession session, Model model) {
        String pending = (String) session.getAttribute(RegistrationSessionKeys.PENDING_EMAIL);
        if (!StringUtils.hasText(pending)) {
            return "redirect:/register/candidat/email";
        }
        model.addAttribute("pendingEmail", pending);
        return "pages/public/register-candidat-step-code";
    }

    @PostMapping("/register/candidat/code")
    public String registerCandidatCodeSubmit(
            @RequestParam("code") String code,
            HttpSession session,
            RedirectAttributes ra
    ) {
        String pending = (String) session.getAttribute(RegistrationSessionKeys.PENDING_EMAIL);
        if (!StringUtils.hasText(pending)) {
            return "redirect:/register/candidat/email";
        }
        Optional<SignupSessionVerified> ok =
                signupEmailVerificationService.verifyAndConsume(pending, ROLE_CANDIDAT, code);
        if (ok.isEmpty()) {
            ra.addFlashAttribute("signupCodeError", true);
            return "redirect:/register/candidat/code";
        }
        session.removeAttribute(RegistrationSessionKeys.PENDING_EMAIL);
        session.setAttribute(RegistrationSessionKeys.VERIFIED, ok.get());
        return "redirect:/register/candidat";
    }

    /* ---------- Recruteur ---------- */

    @GetMapping("/register/recruteur")
    public String registerRecruteurFinish(
            @RequestParam(value = "registerError", required = false) String registerError,
            @RequestParam(value = "code", required = false) String registerErrorCode,
            @RequestParam(value = "detail", required = false) String registerErrorDetail,
            HttpSession session,
            Model model
    ) {
        SignupSessionVerified v = verifiedOrNull(session, ROLE_RECRUTEUR);
        if (v == null) {
            return "redirect:/register/recruteur/email";
        }
        model.addAttribute("signupNom", v.getNomComplet());
        model.addAttribute("signupEmail", v.getEmail());
        boolean err = registerError != null && !registerError.isBlank();
        model.addAttribute("registerErrorFlag", err);
        model.addAttribute("registerErrorCode", err && registerErrorCode != null ? registerErrorCode : "");
        model.addAttribute("registerErrorDetail", err && registerErrorDetail != null ? registerErrorDetail : "");
        return "pages/public/register-recruteur";
    }

    @GetMapping("/register/recruteur/email")
    public String registerRecruteurEmail(HttpSession session) {
        session.removeAttribute(RegistrationSessionKeys.VERIFIED);
        return "pages/public/register-recruteur-step-email";
    }

    @PostMapping("/register/recruteur/email")
    public String registerRecruteurEmailSubmit(
            @RequestParam("nomComplet") String nom,
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes ra
    ) {
        String result = signupEmailVerificationService.requestCode(email, nom, ROLE_RECRUTEUR);
        if ("email_taken".equals(result)) {
            ra.addFlashAttribute("signupEmailError", "taken");
            return "redirect:/register/recruteur/email";
        }
        if ("invalid".equals(result)) {
            ra.addFlashAttribute("signupEmailError", "invalid");
            return "redirect:/register/recruteur/email";
        }
        session.setAttribute(RegistrationSessionKeys.PENDING_EMAIL, email.trim().toLowerCase());
        session.setAttribute("hirehubPendingSignupRole", ROLE_RECRUTEUR);
        ra.addFlashAttribute("signupCodeSent", true);
        return "redirect:/register/recruteur/code";
    }

    @GetMapping("/register/recruteur/code")
    public String registerRecruteurCode(HttpSession session, Model model) {
        String pending = (String) session.getAttribute(RegistrationSessionKeys.PENDING_EMAIL);
        if (!StringUtils.hasText(pending)) {
            return "redirect:/register/recruteur/email";
        }
        model.addAttribute("pendingEmail", pending);
        return "pages/public/register-recruteur-step-code";
    }

    @PostMapping("/register/recruteur/code")
    public String registerRecruteurCodeSubmit(
            @RequestParam("code") String code,
            HttpSession session,
            RedirectAttributes ra
    ) {
        String pending = (String) session.getAttribute(RegistrationSessionKeys.PENDING_EMAIL);
        if (!StringUtils.hasText(pending)) {
            return "redirect:/register/recruteur/email";
        }
        Optional<SignupSessionVerified> ok =
                signupEmailVerificationService.verifyAndConsume(pending, ROLE_RECRUTEUR, code);
        if (ok.isEmpty()) {
            ra.addFlashAttribute("signupCodeError", true);
            return "redirect:/register/recruteur/code";
        }
        session.removeAttribute(RegistrationSessionKeys.PENDING_EMAIL);
        session.setAttribute(RegistrationSessionKeys.VERIFIED, ok.get());
        return "redirect:/register/recruteur";
    }

    private static SignupSessionVerified verifiedOrNull(HttpSession session, String expectedRole) {
        Object raw = session.getAttribute(RegistrationSessionKeys.VERIFIED);
        if (!(raw instanceof SignupSessionVerified v)) {
            return null;
        }
        if (!expectedRole.equalsIgnoreCase(v.getRole())) {
            return null;
        }
        return v;
    }
}
