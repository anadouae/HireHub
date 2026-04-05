package com.hirehub.frontend.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Simulation des états UI (en attendant JWT + session réels).
 * <p>
 * {@code ?demo=} : {@code visiteur} | {@code candidat} | {@code recruteur_pending} | {@code recruteur} | {@code admin}
 * </p>
 * <ul>
 *   <li>{@code recruteur_pending} — compte inscrit comme recruteur, espace visible mais fonctionnalités verrouillées.</li>
 *   <li>{@code recruteur} — recruteur approuvé par l’admin.</li>
 * </ul>
 */
@ControllerAdvice(basePackages = "com.hirehub.frontend.web")
public class DemoUiAdvice {

    @ModelAttribute
    public void uiDemoState(HttpServletRequest request, Model model) {
        String demo = request.getParameter("demo");
        if (demo == null || demo.isBlank()) {
            demo = "candidat";
        }

        boolean visiteur = "visiteur".equals(demo);
        boolean connecte = !visiteur;
        boolean estAdmin = "admin".equals(demo);
        boolean recruteurPending = "recruteur_pending".equals(demo);
        boolean recruteurApprouve = "recruteur".equals(demo);

        String typeCompte;
        if (visiteur) {
            typeCompte = "VISITEUR";
        } else if (estAdmin) {
            typeCompte = "ADMIN";
        } else if (recruteurPending || recruteurApprouve) {
            typeCompte = "RECRUTEUR";
        } else {
            typeCompte = "CANDIDAT";
        }

        boolean menuRecruteur = "RECRUTEUR".equals(typeCompte);
        boolean menuCandidat = "CANDIDAT".equals(typeCompte);
        boolean recruteurFonctionnel = recruteurApprouve;

        model.addAttribute("uiConnecte", connecte);
        model.addAttribute("uiEstAdmin", estAdmin);
        model.addAttribute("uiTypeCompte", typeCompte);
        model.addAttribute("uiMenuCandidat", connecte && menuCandidat);
        model.addAttribute("uiMenuRecruteur", connecte && menuRecruteur);
        model.addAttribute("uiRecruteurPending", connecte && menuRecruteur && !recruteurFonctionnel);
        model.addAttribute("uiRecruteurApprouve", connecte && recruteurFonctionnel);
        model.addAttribute("uiDemoMode", demo);
    }
}
