package com.hirehub.frontend.offre;

public class OffreView {

    private Long id;
    private String titre;
    private String description;
    private String typeContrat;
    private String ville;
    private Double salaire;
    private String dateCreation;
    private String dateExpiration;
    private String statut;
    private String recruteurId;
    private String recruteurEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Double getSalaire() {
        return salaire;
    }

    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getRecruteurId() {
        return recruteurId;
    }

    public void setRecruteurId(String recruteurId) {
        this.recruteurId = recruteurId;
    }

    public String getRecruteurEmail() {
        return recruteurEmail;
    }

    public void setRecruteurEmail(String recruteurEmail) {
        this.recruteurEmail = recruteurEmail;
    }
}
