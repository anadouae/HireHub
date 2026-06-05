package com.hirehub.frontend.recruteur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardCandidatureItem {

    private String candidatureId;
    private String offreId;
    private String offreTitre;
    private String candidatDisplayName;
    private String candidatEmail;
    private String status;
    private String statusLabel;
    private LocalDateTime dateSoumission;

    public String getCandidatureId() {
        return candidatureId;
    }

    public void setCandidatureId(String candidatureId) {
        this.candidatureId = candidatureId;
    }

    public String getOffreId() {
        return offreId;
    }

    public void setOffreId(String offreId) {
        this.offreId = offreId;
    }

    public String getOffreTitre() {
        return offreTitre;
    }

    public void setOffreTitre(String offreTitre) {
        this.offreTitre = offreTitre;
    }

    public String getCandidatDisplayName() {
        return candidatDisplayName;
    }

    public void setCandidatDisplayName(String candidatDisplayName) {
        this.candidatDisplayName = candidatDisplayName;
    }

    public String getCandidatEmail() {
        return candidatEmail;
    }

    public void setCandidatEmail(String candidatEmail) {
        this.candidatEmail = candidatEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public String getDateSoumissionFormatted() {
        if (dateSoumission == null) {
            return "-";
        }
        return dateSoumission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
