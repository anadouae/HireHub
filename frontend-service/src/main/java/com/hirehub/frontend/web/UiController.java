package com.hirehub.frontend.web;

import com.hirehub.frontend.admin.AdminDashboardStats;
import com.hirehub.frontend.admin.AdminSpaceService;
import com.hirehub.frontend.admin.AdminUserDetailVm;
import com.hirehub.frontend.entretien.EntretienFrontendClient;
import com.hirehub.frontend.offre.OffreForm;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffreView;
import com.hirehub.frontend.recruteur.RecruteurStatsService;
import com.hirehub.frontend.recruteur.RecruteurStatsView;
import com.hirehub.common.enums.UserRole;
import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.candidature.ApplicationUploadService;
import com.hirehub.frontend.candidature.CandidateDisplayService;
import com.hirehub.frontend.candidature.CandidateProfileService;
import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.candidature.CandidatureServiceException;
import com.hirehub.frontend.entretien.EntretienView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;


/**
 * Routes UI Thymeleaf — pages publiques, candidat, recruteur et admin.
 */
@Controller
public class UiController {

    private static final Logger log = LoggerFactory.getLogger(UiController.class);

    private final AdminSpaceService adminSpaceService;
    private final OffreFrontendClient offreFrontendClient;
    private final EntretienFrontendClient entretienFrontendClient;
    private final RecruteurStatsService recruteurStatsService;
    private final CandidatureFrontendClient candidatureFrontendClient;
    private final ApplicationUploadService applicationUploadService;
    private final CandidateProfileService candidateProfileService;
    private final CandidateDisplayService candidateDisplayService;

    public UiController(
            AdminSpaceService adminSpaceService,
            OffreFrontendClient offreFrontendClient,
            EntretienFrontendClient entretienFrontendClient,
            RecruteurStatsService recruteurStatsService,
            CandidatureFrontendClient candidatureFrontendClient,
            ApplicationUploadService applicationUploadService,
            CandidateProfileService candidateProfileService,
            CandidateDisplayService candidateDisplayService
    ) {
        this.adminSpaceService = adminSpaceService;
        this.offreFrontendClient = offreFrontendClient;
        this.entretienFrontendClient = entretienFrontendClient;
        this.recruteurStatsService = recruteurStatsService;
        this.candidatureFrontendClient = candidatureFrontendClient;
        this.applicationUploadService = applicationUploadService;
        this.candidateProfileService = candidateProfileService;
        this.candidateDisplayService = candidateDisplayService;
    }

    /* ---------- Public ---------- */

    @GetMapping("/offres")
    public String offres(
            @RequestParam(value = "ville", required = false) String ville,
            @RequestParam(value = "typeContrat", required = false) String typeContrat,
            @RequestParam(value = "motCle", required = false) String motCle,
            Model model
    ) {
        model.addAttribute("offresPage", offreFrontendClient.offresPubliees(ville, typeContrat, motCle));
        model.addAttribute("filtreVille", ville != null ? ville : "");
        model.addAttribute("filtreTypeContrat", typeContrat != null ? typeContrat : "");
        model.addAttribute("filtreMotCle", motCle != null ? motCle : "");
        return "pages/public/offres";
    }

    @GetMapping("/offres/{id}")
    public String offreDetail(@PathVariable("id") Long id, Model model) {
        OffreView offre = offreFrontendClient.detail(id);
        if (offre == null) {
            return "redirect:/offres?error=not_found";
        }
        model.addAttribute("offre", offre);
        model.addAttribute("offreId", offre.getId());
        return "pages/public/offre-detail";
    }

    @GetMapping("/offres/{id}/postuler")
    public String postuler(@PathVariable("id") Long id, Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof HirehubUserDetails user
                && user.getRole() != UserRole.CANDIDAT) {
            return "redirect:/offres/" + id + "?error=postuler_candidat_only";
        }
        model.addAttribute("offreId", id);
        boolean hasDefaultCv = false;
        if (auth != null && auth.getPrincipal() instanceof HirehubUserDetails user) {
            hasDefaultCv = candidateProfileService.defaultCvPathForApply(user.getId()) != null;
        }
        model.addAttribute("hasDefaultCv", hasDefaultCv);
        return "pages/public/postuler";
    }

    @PostMapping("/offres/{id}/postuler")
    public String postulerSubmit(
            @PathVariable("id") Long id,
            @RequestParam(value = "lettre", required = false) String lettre,
            @RequestParam(value = "cv", required = false) MultipartFile cv,
            @RequestParam(value = "useDefaultCv", defaultValue = "false") boolean useDefaultCv,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        if (auth == null || !(auth.getPrincipal() instanceof HirehubUserDetails user)) {
            return "redirect:/login?flow=candidat";
        }
        if (user.getRole() != UserRole.CANDIDAT) {
            redirectAttributes.addFlashAttribute("postulerError", "Seuls les comptes candidat peuvent postuler.");
            return "redirect:/offres/" + id;
        }
        try {
            String cvPath;
            if (useDefaultCv) {
                cvPath = candidateProfileService.defaultCvPathForApply(user.getId());
                if (cvPath == null) {
                    throw new IllegalArgumentException("Aucun CV par défaut enregistré. Ajoutez-en un dans Mon profil ou téléversez un fichier.");
                }
            } else {
                cvPath = applicationUploadService.storeCv(user.getId(), id.toString(), cv);
            }
            String lettrePath = applicationUploadService.storeLettre(user.getId(), id.toString(), lettre);
            candidatureFrontendClient.create(id.toString(), cvPath, lettrePath, user);
            redirectAttributes.addFlashAttribute("success", "Votre candidature a bien été envoyée.");
            return "redirect:/candidat/mes-candidatures";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("postulerError", ex.getMessage());
            return "redirect:/offres/" + id + "/postuler";
        } catch (CandidatureServiceException ex) {
            redirectAttributes.addFlashAttribute("postulerError", ex.getMessage());
            return "redirect:/offres/" + id + "/postuler";
        } catch (Exception ex) {
            log.error("Postulation offre {}", id, ex);
            redirectAttributes.addFlashAttribute("postulerError", "Envoi impossible pour le moment. Réessayez plus tard.");
            return "redirect:/offres/" + id + "/postuler";
        }
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "flow", required = false) String flow,
            @RequestParam(value = "docMatch", required = false) String docMatch,
            @RequestParam(value = "registerError", required = false) String registerError,
            @RequestParam(value = "code", required = false) String registerErrorCode,
            @RequestParam(value = "oauth_error", required = false) String oauthError,
            HttpServletRequest request,
            Model model
    ) {
        model.addAttribute("loginRegistered", registered != null && !registered.isBlank());
        model.addAttribute("loginFlow", flow != null ? flow : "");
        model.addAttribute("loginDocMatch", "1".equals(docMatch));
        model.addAttribute("loginRegisterError", registerError != null && !registerError.isBlank());
        model.addAttribute("loginRegisterErrorCode", registerErrorCode != null ? registerErrorCode : "");
        boolean oauthNotRegistered = "not_registered".equals(oauthError);
        boolean oauthErr = oauthError != null && !oauthError.isBlank() && !oauthNotRegistered;
        model.addAttribute("loginOauthError", oauthErr);
        model.addAttribute("loginOauthNotRegistered", oauthNotRegistered);
        boolean errorParamPresent = request.getParameterMap().containsKey("error");
        String errorVal = request.getParameter("error");
        boolean blocked = "blocked".equals(errorVal);
        model.addAttribute("loginBlocked", blocked);
        model.addAttribute("loginCredentialError", errorParamPresent && !blocked && !oauthErr);
        return "pages/public/login";
    }

    @GetMapping("/register")
    public String register() {
        return "pages/public/register";
    }

    @GetMapping("/mes-candidatures")
    public String legacyMesCandidatures() {
        return "redirect:/candidat/mes-candidatures";
    }

    /* ---------- Candidat ---------- */

    @GetMapping("/candidat/entretiens")
    public String candidatEntretiens(Model model, org.springframework.security.core.Authentication auth) {
        try {
            if (auth != null && auth.getPrincipal() instanceof HirehubUserDetails details) {
                model.addAttribute("entretiens", entretienFrontendClient.listForCandidat(details));
            } else {
                model.addAttribute("entretiens", java.util.List.of());
            }
            model.addAttribute("apiError", false);
        } catch (Exception ex) {
            log.warn("Entretiens candidat: {}", ex.getMessage());
            model.addAttribute("entretiens", java.util.List.of());
            model.addAttribute("apiError", true);
        }
        return "pages/candidat/entretiens";
    }

    @GetMapping("/candidat/profil")
    public String candidatProfil(Model model, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof HirehubUserDetails user)) {
            return "redirect:/login?flow=candidat";
        }
        model.addAttribute("profile", candidateProfileService.load(user));
        return "pages/candidat/profil";
    }

    @PostMapping("/candidat/profil")
    public String candidatProfilSave(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "defaultCv", required = false) MultipartFile defaultCv,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        if (auth == null || !(auth.getPrincipal() instanceof HirehubUserDetails user)) {
            return "redirect:/login?flow=candidat";
        }
        try {
            candidateProfileService.save(user, phone, defaultCv);
            redirectAttributes.addFlashAttribute("success", "Profil enregistré.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            log.error("Enregistrement profil candidat", ex);
            redirectAttributes.addFlashAttribute("error", "Impossible d'enregistrer le profil.");
        }
        return "redirect:/candidat/profil";
    }

    @GetMapping("/demande-recruteur")
    public String legacyDemandeRecruteur() {
        return "redirect:/register/recruteur";
    }

    /* ---------- Recruteur ---------- */

    @GetMapping("/recruteur/dashboard")
    public String recruteurDashboard(Model model) {
        try {
            var offresPage = offreFrontendClient.mesOffres();
            model.addAttribute("offresCount", offresPage.getTotalElements());
            model.addAttribute("offresRecentes", offresPage.getContent());
        } catch (Exception ex) {
            log.warn("Dashboard recruteur: impossible de charger les offres: {}", ex.getMessage());
            model.addAttribute("offresCount", 0);
            model.addAttribute("offresRecentes", java.util.List.of());
        }
        return "pages/recruteur/dashboard";
    }

    @GetMapping("/recruteur/offres")
    public String recruteurOffres(
            @RequestParam(value = "created", required = false) String created,
            @RequestParam(value = "updated", required = false) String updated,
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        try {
            model.addAttribute("offresPage", offreFrontendClient.mesOffres());
            model.addAttribute("offreApiError", false);
        } catch (Exception ex) {
            log.warn("Liste offres recruteur: {}", ex.getMessage());
            model.addAttribute("offresPage", new com.hirehub.frontend.offre.OffrePageResponse());
            model.addAttribute("offreApiError", true);
        }
        model.addAttribute("offreCreated", created != null);
        model.addAttribute("offreUpdated", updated != null);
        model.addAttribute("offreError", error != null);
        return "pages/recruteur/offres-list";
    }

    @GetMapping("/recruteur/offres/nouvelle")
    public String recruteurOffreNouvelle(Model model) {
        model.addAttribute("offreForm", new OffreForm());
        return "pages/recruteur/offre-form";
    }

    @PostMapping("/recruteur/offres")
    public String recruteurCreerOffre(@ModelAttribute OffreForm offreForm) {
        try {
            offreFrontendClient.creer(offreForm);
            return "redirect:/recruteur/offres?created=1";
        } catch (Exception ex) {
            log.warn("Recruiter offer creation failed: {}", ex.getMessage());
            return "redirect:/recruteur/offres?error=action_failed";
        }
    }

    @PostMapping("/recruteur/offres/{id}/publier")
    public String recruteurPublierOffre(@PathVariable("id") Long id) {
        try {
            offreFrontendClient.publier(id);
            return "redirect:/recruteur/offres?updated=1";
        } catch (Throwable ex) {
            log.warn("Recruiter offer publish failed id={}: {}", id, ex.toString());
            return "redirect:/recruteur/offres?error=action_failed";
        }
    }

    @PostMapping("/recruteur/offres/{id}/fermer")
    public String recruteurFermerOffre(@PathVariable("id") Long id) {
        try {
            offreFrontendClient.fermer(id);
            return "redirect:/recruteur/offres?updated=1";
        } catch (Throwable ex) {
            log.warn("Recruiter offer close failed id={}: {}", id, ex.toString());
            return "redirect:/recruteur/offres?error=action_failed";
        }
    }

    @GetMapping("/recruteur/offres/{id}/pipeline")
    public String recruteurPipelineLegacy(@PathVariable("id") Long id) {
        return "redirect:/recruteur/pipeline/" + id;
    }

    @GetMapping("/recruteur/entretiens")
    public String recruteurEntretiens(Model model) {
        try {
            var entretiens = entretienFrontendClient.listForRecruiter();
            for (EntretienView e : entretiens) {
                e.setCandidatDisplayName(candidateDisplayService.displayName(e.getCandidatId()));
            }
            model.addAttribute("entretiens", entretiens);
            model.addAttribute("apiError", false);
        } catch (Exception ex) {
            log.warn("Entretiens recruteur: {}", ex.getMessage());
            model.addAttribute("entretiens", java.util.List.of());
            model.addAttribute("apiError", true);
        }
        return "pages/recruteur/entretiens";
    }

    @GetMapping("/recruteur/statistiques")
    public String recruteurStatistiques(Model model) {
        try {
            RecruteurStatsView stats = recruteurStatsService.compute();
            model.addAttribute("stats", stats);
            model.addAttribute("apiError", false);
        } catch (Exception ex) {
            log.warn("Statistiques recruteur: {}", ex.getMessage());
            model.addAttribute("stats", new RecruteurStatsView());
            model.addAttribute("apiError", true);
        }
        return "pages/recruteur/statistiques";
    }

    /* ---------- Admin ---------- */

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        AdminDashboardStats stats = adminSpaceService.dashboardStats();
        model.addAttribute("stats", stats);
        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/utilisateurs")
    public String adminUtilisateurs(Model model) {
        model.addAttribute("users", adminSpaceService.allUsers());
        return "pages/admin/utilisateurs";
    }

    /**
     * Chemins plus spécifiques en premier (évite toute ambiguïté de matching avec {@code /{id}}).
     * GET sur une URL d'action : rediriger (la vraie action est en POST depuis la liste).
     */
    @GetMapping("/admin/utilisateurs/{id}/delete")
    public String adminDeleteUserWrongHttpMethod(@PathVariable("id") String id) {
        return "redirect:/admin/utilisateurs?error=action_failed";
    }

    @GetMapping("/admin/utilisateurs/{id}/block")
    public String adminBlockUserWrongHttpMethod(@PathVariable("id") String id) {
        return "redirect:/admin/utilisateurs?error=action_failed";
    }

    @GetMapping("/admin/utilisateurs/{id}/unblock")
    public String adminUnblockUserWrongHttpMethod(@PathVariable("id") String id) {
        return "redirect:/admin/utilisateurs?error=action_failed";
    }

    @GetMapping("/admin/utilisateurs/{id}")
    public String adminUtilisateurDetails(@PathVariable("id") String id, Model model) {
        try {
            final UUID userId;
            try {
                userId = UUID.fromString(id);
            } catch (IllegalArgumentException ex) {
                return "redirect:/admin/utilisateurs?error=not_found";
            }
            return adminSpaceService.findUser(userId)
                    .map(user -> {
                        AdminUserDetailVm vm = AdminUserDetailVm.from(user);
                        model.addAttribute("detailEmail", vm.getEmail());
                        model.addAttribute("detailFullName", vm.getFullName());
                        model.addAttribute("detailRole", vm.getRole());
                        model.addAttribute("detailBlocked", vm.isBlocked());
                        return "pages/admin/utilisateur-detail";
                    })
                    .orElse("redirect:/admin/utilisateurs?error=not_found");
        } catch (Throwable ex) {
            log.warn("Admin user detail failed id={}: {}", id, ex.toString());
            return "redirect:/admin/utilisateurs?error=not_found";
        }
    }

    @GetMapping("/admin/logs")
    public String adminLogs(Model model) {
        model.addAttribute("stats", adminSpaceService.dashboardStats());
        return "pages/admin/logs";
    }

    @GetMapping("/admin/demandes-recruteur")
    public String adminDemandesRecruteur(Model model) {
        model.addAttribute("recruiters", adminSpaceService.recruiters());
        return "pages/admin/demandes-recruteur";
    }

    @PostMapping("/admin/utilisateurs/{id}/block")
    public String adminBlockUser(@PathVariable("id") String id) {
        try {
            adminSpaceService.blockUser(UUID.fromString(id));
            return "redirect:/admin/utilisateurs?updated=1";
        } catch (Throwable ex) {
            log.warn("Admin block user failed id={}: {}", id, ex.toString());
            return "redirect:/admin/utilisateurs?error=action_failed";
        }
    }

    @PostMapping("/admin/utilisateurs/{id}/unblock")
    public String adminUnblockUser(@PathVariable("id") String id) {
        try {
            adminSpaceService.unblockUser(UUID.fromString(id));
            return "redirect:/admin/utilisateurs?updated=1";
        } catch (Throwable ex) {
            log.warn("Admin unblock user failed id={}: {}", id, ex.toString());
            return "redirect:/admin/utilisateurs?error=action_failed";
        }
    }

    @PostMapping("/admin/utilisateurs/{id}/delete")
    public String adminDeleteUser(@PathVariable("id") String id) {
        try {
            adminSpaceService.deleteUser(UUID.fromString(id));
            return "redirect:/admin/utilisateurs?updated=1";
        } catch (Throwable ex) {
            log.warn("Admin delete user failed id={}: {}", id, ex.toString());
            return "redirect:/admin/utilisateurs?error=action_failed";
        }
    }

    @GetMapping("/support")
    public String supportPage() {
        return "pages/public/support";
    }

    @GetMapping("/faq")
    public String faqPage() {
        return "pages/public/faq";
    }

    @GetMapping("/confidentialite")
    public String confidentialitePage() {
        return "pages/public/confidentialite";
    }
}
