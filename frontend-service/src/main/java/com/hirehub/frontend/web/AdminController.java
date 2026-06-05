package com.hirehub.frontend.web;

import com.hirehub.frontend.admin.AdminOffreRow;
import com.hirehub.frontend.admin.AdminPlatformService;
import com.hirehub.frontend.admin.AdminPlatformStats;
import com.hirehub.frontend.admin.AdminSpaceService;
import com.hirehub.frontend.admin.AdminUserDetailVm;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.entretien.EntretienAdminPageResponse;
import com.hirehub.frontend.offre.OffrePageResponse;
import com.hirehub.frontend.offre.OffreView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminSpaceService adminSpaceService;
    private final AdminPlatformService adminPlatformService;

    public AdminController(AdminSpaceService adminSpaceService, AdminPlatformService adminPlatformService) {
        this.adminSpaceService = adminSpaceService;
        this.adminPlatformService = adminPlatformService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminPlatformService.userStats());
        model.addAttribute("platform", adminPlatformService.platformStats());
        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/offres")
    public String offres(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String typeContrat,
            @RequestParam(required = false) String motCle,
            @RequestParam(required = false) String recruteurEmail,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        List<AdminOffreRow> rows = adminPlatformService.listOffres(statut, ville, typeContrat, motCle, recruteurEmail, page);
        OffrePageResponse meta = adminPlatformService.offresPageMeta(statut, ville, typeContrat, motCle, recruteurEmail, page);
        model.addAttribute("rows", rows);
        model.addAttribute("page", meta);
        model.addAttribute("statut", statut);
        model.addAttribute("ville", ville);
        model.addAttribute("typeContrat", typeContrat);
        model.addAttribute("motCle", motCle);
        model.addAttribute("recruteurEmail", recruteurEmail);
        return "pages/admin/offres";
    }

    @GetMapping("/admin/offres/{id}")
    public String offreDetail(@PathVariable Long id, Model model) {
        OffreView offre = adminPlatformService.offreDetail(id);
        if (offre == null) {
            return "redirect:/admin/offres?error=not_found";
        }
        List<CandidatureDTO> candidatures = adminPlatformService.candidaturesForOffre(String.valueOf(id));
        model.addAttribute("offre", offre);
        model.addAttribute("candidatures", candidatures);
        model.addAttribute("readOnly", true);
        return "pages/admin/offre-detail";
    }

    @GetMapping("/admin/entretiens")
    public String entretiens(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String recruteurId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        EntretienAdminPageResponse entretiens = adminPlatformService.listEntretiens(status, type, recruteurId, page);
        model.addAttribute("entretiens", entretiens);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("recruteurId", recruteurId);
        AdminPlatformStats platform = adminPlatformService.platformStats();
        model.addAttribute("platform", platform);
        return "pages/admin/entretiens";
    }

    @GetMapping("/admin/utilisateurs")
    public String utilisateurs(Model model) {
        model.addAttribute("users", adminSpaceService.allUsers());
        return "pages/admin/utilisateurs";
    }

    @GetMapping("/admin/utilisateurs/{id}/delete")
    public String deleteUserWrongMethod() {
        return "redirect:/admin/utilisateurs?error=action_failed";
    }

    @GetMapping("/admin/utilisateurs/{id}/block")
    public String blockUserWrongMethod() {
        return "redirect:/admin/utilisateurs?error=action_failed";
    }

    @GetMapping("/admin/utilisateurs/{id}/unblock")
    public String unblockUserWrongMethod() {
        return "redirect:/admin/utilisateurs?error=action_failed";
    }

    @GetMapping("/admin/utilisateurs/{id}")
    public String utilisateurDetail(@PathVariable String id, Model model) {
        try {
            UUID userId = UUID.fromString(id);
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
        } catch (Exception ex) {
            log.warn("Admin user detail failed id={}: {}", id, ex.toString());
            return "redirect:/admin/utilisateurs?error=not_found";
        }
    }

    @PostMapping("/admin/utilisateurs/{id}/block")
    public String blockUser(@PathVariable String id) {
        try {
            adminSpaceService.blockUser(UUID.fromString(id));
            return "redirect:/admin/utilisateurs?updated=1";
        } catch (Throwable ex) {
            return "redirect:/admin/utilisateurs?error=action_failed";
        }
    }

    @PostMapping("/admin/utilisateurs/{id}/unblock")
    public String unblockUser(@PathVariable String id) {
        try {
            adminSpaceService.unblockUser(UUID.fromString(id));
            return "redirect:/admin/utilisateurs?updated=1";
        } catch (Throwable ex) {
            return "redirect:/admin/utilisateurs?error=action_failed";
        }
    }

    @PostMapping("/admin/utilisateurs/{id}/delete")
    public String deleteUser(@PathVariable String id) {
        try {
            adminSpaceService.deleteUser(UUID.fromString(id));
            return "redirect:/admin/utilisateurs?updated=1";
        } catch (Throwable ex) {
            return "redirect:/admin/utilisateurs?error=action_failed";
        }
    }
}
