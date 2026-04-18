package com.hirehub.auth.dto;

public class RegisterCandidateForm {

    private String nomComplet;
    private String email;
    private String password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

