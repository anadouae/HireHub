package com.hirehub.common.events;

public class RecruiterRegisteredEvent {
    private String userId;
    private String email;
    private String nomComplet;
    private String raisonSociale;
    private String siret;
    private String presentation;
    private String justificatifNom;
    private String justificatifContentType;
    private String justificatifBase64;

    public RecruiterRegisteredEvent() {
    }

    public RecruiterRegisteredEvent(String userId, String email, String nomComplet, String raisonSociale, String siret,
                                    String presentation, String justificatifNom, String justificatifContentType,
                                    String justificatifBase64) {
        this.userId = userId;
        this.email = email;
        this.nomComplet = nomComplet;
        this.raisonSociale = raisonSociale;
        this.siret = siret;
        this.presentation = presentation;
        this.justificatifNom = justificatifNom;
        this.justificatifContentType = justificatifContentType;
        this.justificatifBase64 = justificatifBase64;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public String getSiret() {
        return siret;
    }

    public String getPresentation() {
        return presentation;
    }

    public String getJustificatifNom() {
        return justificatifNom;
    }

    public String getJustificatifContentType() {
        return justificatifContentType;
    }

    public String getJustificatifBase64() {
        return justificatifBase64;
    }
}
