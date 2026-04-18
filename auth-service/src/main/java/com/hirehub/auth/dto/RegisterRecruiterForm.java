package com.hirehub.auth.dto;

import org.springframework.web.multipart.MultipartFile;

public class RegisterRecruiterForm {

    private String nomComplet;
    private String email;
    private String telephone;
    private String password;
    private String raisonSociale;
    private String siret;
    private String ville;
    private String siteWeb;
    private String presentation;
    private MultipartFile justificatifEntreprise;
    private String frontendRedirect;
    private String recaptchaToken;

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public MultipartFile getJustificatifEntreprise() {
        return justificatifEntreprise;
    }

    public void setJustificatifEntreprise(MultipartFile justificatifEntreprise) {
        this.justificatifEntreprise = justificatifEntreprise;
    }

    public String getFrontendRedirect() {
        return frontendRedirect;
    }

    public void setFrontendRedirect(String frontendRedirect) {
        this.frontendRedirect = frontendRedirect;
    }

    public String getRecaptchaToken() {
        return recaptchaToken;
    }

    public void setRecaptchaToken(String recaptchaToken) {
        this.recaptchaToken = recaptchaToken;
    }
}

