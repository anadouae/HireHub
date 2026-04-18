package com.hirehub.auth.web;

import com.hirehub.auth.dto.RegisterCandidateForm;
import com.hirehub.auth.dto.RegisterRecruiterForm;
import com.hirehub.auth.exception.RecruiterJustificatifMismatchException;
import com.hirehub.auth.model.UserAccount;
import com.hirehub.auth.service.RecaptchaVerificationService;
import com.hirehub.auth.service.RecruiterDocumentStrictValidationService;
import com.hirehub.auth.service.UserRegistrationService;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.RecruiterVerificationStatus;
import com.hirehub.common.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRegistrationService userRegistrationService;
    private final RecaptchaVerificationService recaptchaVerificationService;
    private final RecruiterDocumentStrictValidationService recruiterDocumentStrictValidationService;
    private final String frontendBaseUrl;

    public AuthController(
            UserRegistrationService userRegistrationService,
            RecaptchaVerificationService recaptchaVerificationService,
            RecruiterDocumentStrictValidationService recruiterDocumentStrictValidationService,
            @Value("${hirehub.frontend-base-url}") String frontendBaseUrl
    ) {
        this.userRegistrationService = userRegistrationService;
        this.recaptchaVerificationService = recaptchaVerificationService;
        this.recruiterDocumentStrictValidationService = recruiterDocumentStrictValidationService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @PostMapping(
            path = "/register/candidat",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> registerCandidate(@ModelAttribute RegisterCandidateForm form) {
        if (!recaptchaVerificationService.isValid(form.getRecaptchaToken())) {
            if (wantsFrontendRedirect(form.getFrontendRedirect())) {
                return ResponseEntity.status(HttpStatus.FOUND).location(registerErrorRedirect("recaptcha_failed")).build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Validation reCAPTCHA echouee."));
        }
        if (!hasText(form.getNomComplet()) || !hasText(form.getEmail()) || !hasText(form.getPassword())) {
            if (wantsFrontendRedirect(form.getFrontendRedirect())) {
                return ResponseEntity.status(HttpStatus.FOUND).location(registerErrorRedirect("missing_fields")).build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Champs obligatoires manquants (nom, email, mot de passe)."));
        }

        try {
            UserAccount user = userRegistrationService.registerCandidate(form);
            if (wantsFrontendRedirect(form.getFrontendRedirect())) {
                return ResponseEntity.status(HttpStatus.FOUND).location(registerSuccessRedirect("candidat")).build();
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user.getId().toString());
            payload.put("email", user.getEmail());
            payload.put("role", UserRole.CANDIDAT.name());
            payload.put("message", "Compte candidat cree.");
            return ResponseEntity.ok(ApiResponse.ok("Inscription candidat reussie.", payload));
        } catch (IllegalStateException exception) {
            if (wantsFrontendRedirect(form.getFrontendRedirect())) {
                return ResponseEntity.status(HttpStatus.FOUND).location(registerErrorRedirect("email_taken")).build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
        }
    }

    @PostMapping(
            path = "/register/recruteur",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> registerRecruiter(@ModelAttribute RegisterRecruiterForm form) {
        boolean redirect = wantsFrontendRedirect(form.getFrontendRedirect());

        if (!hasText(form.getNomComplet()) || !hasText(form.getEmail()) || !hasText(form.getPassword())
                || !hasText(form.getRaisonSociale()) || !hasText(form.getPresentation())) {
            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND).location(recruiterRegisterErrorRedirect("missing_fields")).build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Champs obligatoires manquants pour l'inscription recruteur."));
        }
        if (form.getJustificatifEntreprise() == null || form.getJustificatifEntreprise().isEmpty()) {
            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND).location(recruiterRegisterErrorRedirect("missing_file")).build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Le justificatif entreprise est obligatoire."));
        }

        /*
         * reCAPTCHA avant l'OCR : le jeton est emis au moment du clic ; si l'appel OCR dure plusieurs
         * dizaines de secondes puis on verifie le jeton, Google le refuse souvent (TTL expire) alors que
         * le formulaire et le document sont corrects.
         */
        if (!recaptchaVerificationService.isValid(form.getRecaptchaToken())) {
            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND).location(recruiterRegisterErrorRedirect("recaptcha_failed")).build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("Validation reCAPTCHA echouee."));
        }

        final boolean documentVerifiedNoMismatch;
        try {
            recruiterDocumentStrictValidationService.validateOrThrow(form);
            documentVerifiedNoMismatch = recruiterDocumentStrictValidationService.isStrictEnabled();
        } catch (RecruiterJustificatifMismatchException exception) {
            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(recruiterRegisterErrorRedirect("justificatif_mismatch", exception.detailQueryValue()))
                        .build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            if (redirect) {
                String code = recruiterJustificatifErrorCode(exception);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(recruiterRegisterErrorRedirect(code))
                        .build();
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
        } catch (IllegalStateException exception) {
            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(recruiterRegisterErrorRedirect("strict_verification_unavailable"))
                        .build();
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.error(exception.getMessage()));
        }

        try {
            UserAccount user = userRegistrationService.registerRecruiter(form);
            String userId = user.getId().toString();
            RecruiterVerificationStatus status = user.getVerificationStatus() == null
                    ? RecruiterVerificationStatus.APPROVED
                    : user.getVerificationStatus();
            String justificatifNom = safeFilename(form.getJustificatifEntreprise());

            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(registerSuccessRedirect("recruteur", documentVerifiedNoMismatch))
                        .build();
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("email", form.getEmail());
            payload.put("role", UserRole.RECRUTEUR.name());
            payload.put("verificationStatus", status.name());
            payload.put("verificationScore", null);
            payload.put("autoApproved", true);
            payload.put("needsAdminReview", false);
            payload.put("justificatifNom", justificatifNom);
            payload.put("decisionMessage", "Inscription validee immediatement. Compte recruteur actif.");
            return ResponseEntity.ok(ApiResponse.ok("Inscription recruteur reussie.", payload));
        } catch (IllegalStateException exception) {
            boolean emailTaken = exception.getMessage() != null
                    && exception.getMessage().toLowerCase().contains("compte existe");
            if (redirect) {
                return ResponseEntity.status(HttpStatus.FOUND).location(
                        recruiterRegisterErrorRedirect(emailTaken ? "email_taken" : "strict_verification_unavailable")
                ).build();
            }
            if (emailTaken) {
                return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.error(exception.getMessage()));
        }
    }

    private static String recruiterJustificatifErrorCode(IllegalArgumentException exception) {
        String m = exception.getMessage() != null ? exception.getMessage() : "";
        if (m.contains("Format justificatif")) {
            return "justificatif_format";
        }
        if (m.contains("OCR") || m.contains("document plus lisible")) {
            return "justificatif_ocr";
        }
        if (m.contains("lire le justificatif")) {
            return "justificatif_lecture";
        }
        return "justificatif_invalide";
    }

    private String safeFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        return StringUtils.hasText(original) ? original : "justificatif-sans-nom";
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private boolean wantsFrontendRedirect(String flag) {
        return "true".equalsIgnoreCase(flag);
    }

    private URI registerSuccessRedirect(String flow) {
        return registerSuccessRedirect(flow, false);
    }

    /**
     * @param documentVerifiedNoMismatch si true (recruteur + mode strict), le front affiche le message
     *                                     de conformite document / formulaire.
     */
    private URI registerSuccessRedirect(String flow, boolean documentVerifiedNoMismatch) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/login")
                .queryParam("registered", "1")
                .queryParam("flow", flow);
        if ("recruteur".equals(flow) && documentVerifiedNoMismatch) {
            builder.queryParam("docMatch", "1");
        }
        return builder.build(true).toUri();
    }

    private URI registerErrorRedirect(String code) {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/login")
                .queryParam("registerError", "1")
                .queryParam("code", code)
                .build(true)
                .toUri();
    }

    /** Erreurs inscription recruteur : rester sur le formulaire (evite confusion avec la page de connexion). */
    private URI recruiterRegisterErrorRedirect(String code) {
        return recruiterRegisterErrorRedirect(code, null);
    }

    private URI recruiterRegisterErrorRedirect(String code, String detail) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/register/recruteur")
                .queryParam("registerError", "1")
                .queryParam("code", code);
        if (StringUtils.hasText(detail)) {
            builder.queryParam("detail", detail);
        }
        return builder.build(true).toUri();
    }
}

