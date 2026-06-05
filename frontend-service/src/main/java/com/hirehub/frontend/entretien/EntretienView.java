package com.hirehub.frontend.entretien;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EntretienView {

    private String id;
    private String candidatureId;
    private String candidatId;
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private String type;
    private String status;
    private String candidatDisplayName;
    private String offreId;
    private String offreTitre;
    private String recruteurLabel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCandidatureId() {
        return candidatureId;
    }

    public void setCandidatureId(String candidatureId) {
        this.candidatureId = candidatureId;
    }

    public String getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(String candidatId) {
        this.candidatId = candidatId;
    }

    public String getRecruteurId() {
        return recruteurId;
    }

    public void setRecruteurId(String recruteurId) {
        this.recruteurId = recruteurId;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getLienVisio() {
        return lienVisio;
    }

    public void setLienVisio(String lienVisio) {
        this.lienVisio = lienVisio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCandidatDisplayName() {
        return candidatDisplayName;
    }

    public void setCandidatDisplayName(String candidatDisplayName) {
        this.candidatDisplayName = candidatDisplayName;
    }

    public String getDateHeureFormatted() {
        if (dateHeure == null) {
            return "-";
        }
        return dateHeure.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
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

    public String getRecruteurLabel() {
        return recruteurLabel;
    }

    public void setRecruteurLabel(String recruteurLabel) {
        this.recruteurLabel = recruteurLabel;
    }

    public String getLieuOuVisio() {
        if (lienVisio != null && !lienVisio.isBlank()) {
            return lienVisio;
        }
        if (lieu != null && !lieu.isBlank()) {
            return lieu;
        }
        return "-";
    }
}
