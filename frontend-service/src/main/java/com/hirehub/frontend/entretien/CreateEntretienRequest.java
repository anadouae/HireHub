package com.hirehub.frontend.entretien;

import java.time.LocalDateTime;

public class CreateEntretienRequest {

    private String candidatureId;
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private String type;
    private String notesInternes;

    public String getCandidatureId() {
        return candidatureId;
    }

    public void setCandidatureId(String candidatureId) {
        this.candidatureId = candidatureId;
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

    public String getNotesInternes() {
        return notesInternes;
    }

    public void setNotesInternes(String notesInternes) {
        this.notesInternes = notesInternes;
    }
}
