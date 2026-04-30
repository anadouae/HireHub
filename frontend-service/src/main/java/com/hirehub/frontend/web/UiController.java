package com.hirehub.frontend.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Routes UI Thymeleaf — toutes les pages par acteur (données mock, branchement API plus tard).
 */
@Controller
public class UiController {

    /* ---------- Public ---------- */

    @GetMapping("/offres")
    public String offres() {
        return "pages/public/offres";
    }

    @GetMapping("/offres/{id}")
    public String offreDetail(@PathVariable Long id, Model model) {
        model.addAttribute("offreId", id);
        return "pages/public/offre-detail";
    }

    @GetMapping("/offres/{id}/postuler")
    public String postuler(@PathVariable Long id, Model model) {
        model.addAttribute("offreId", id);
        return "pages/public/postuler";
    }

    @GetMapping("/login")
    public String login() {
        return "pages/public/login";
    }

    @GetMapping("/register")
    public String register() {
        return "pages/public/register";
    }

    @GetMapping("/register/candidat")
    public String registerCandidat() {
        return "pages/public/register-candidat";
    }

    @GetMapping("/register/recruteur")
    public String registerRecruteur() {
        return "pages/public/register-recruteur";
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
    public String recruteurOffres() {
        return "pages/recruteur/offres-list";
    }

    @GetMapping("/recruteur/offres/nouvelle")
    public String recruteurOffreNouvelle() {
        return "pages/recruteur/offre-form";
    }

    @GetMapping("/recruteur/offres/{id}/pipeline")
    public String recruteurPipeline(@PathVariable Long id, Model model) {
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
    public String adminDashboard() {
        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/utilisateurs")
    public String adminUtilisateurs() {
        return "pages/admin/utilisateurs";
    }

    @GetMapping("/admin/logs")
    public String adminLogs() {
        return "pages/admin/logs";
    }

    @GetMapping("/admin/demandes-recruteur")
    public String adminDemandesRecruteur() {
        return "pages/admin/demandes-recruteur";
    }
}