package com.hirehub.frontend.web;

import com.hirehub.frontend.admin.AdminDashboardStats;
import com.hirehub.frontend.admin.AdminSpaceService;
import com.hirehub.frontend.admin.AdminUserDetailVm;
import com.hirehub.frontend.offre.OffreForm;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffreView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;


/**
 * Routes UI Thymeleaf — toutes les pages par acteur (données mock, branchement API plus tard).
 */
@Controller
public class UiController {

    private static final Logger log = LoggerFactory.getLogger(UiController.class);

    private final AdminSpaceService adminSpaceService;
    private final OffreFrontendClient offreFrontendClient;

    public UiController(AdminSpaceService adminSpaceService, OffreFrontendClient offreFrontendClient) {
        this.adminSpaceService = adminSpaceService;
        this.offreFrontendClient = offreFrontendClient;
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
    public String postuler(@PathVariable("id") Long id, Model model) {
        model.addAttribute("offreId", id);
        return "pages/public/postuler";
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
        boolean oauthErr = oauthError != null && !oauthError.isBlank();
        model.addAttribute("loginOauthError", oauthErr);
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

    @GetMapping("/candidat/dashboard")
    public String candidatDashboard() {
        return "pages/candidat/dashboard";
    }

    @GetMapping("/candidat/mes-candidatures")
    public String candidatMesCandidatures() {
        return "pages/candidat/mes-candidatures";
    }

    @GetMapping("/candidat/entretiens")
    public String candidatEntretiens() {
        return "pages/candidat/entretiens";
    }

    @GetMapping("/candidat/profil")
    public String candidatProfil() {
        return "pages/candidat/profil";
    }

    @GetMapping("/demande-recruteur")
    public String legacyDemandeRecruteur() {
        return "redirect:/register/recruteur";
    }

    /* ---------- Recruteur ---------- */

    @GetMapping("/recruteur/dashboard")
    public String recruteurDashboard() {
        return "pages/recruteur/dashboard";
    }

    @GetMapping("/recruteur/offres")
    public String recruteurOffres(
            @RequestParam(value = "created", required = false) String created,
            @RequestParam(value = "updated", required = false) String updated,
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        model.addAttribute("offresPage", offreFrontendClient.mesOffres());
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
        } catch (Throwable ex) {
            log.warn("Recruiter offer creation failed: {}", ex.toString());
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
    public String recruteurPipeline(@PathVariable("id") Long id, Model model) {
        model.addAttribute("offreId", id);
        return "pages/recruteur/pipeline";
    }

    @GetMapping("/recruteur/entretiens")
    public String recruteurEntretiens() {
        return "pages/recruteur/entretiens";
    }

    @GetMapping("/recruteur/statistiques")
    public String recruteurStatistiques() {
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
