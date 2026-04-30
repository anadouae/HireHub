package com.hirehub.offre.dto;

import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import com.hirehub.offre.entity.Offre;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OffreResponse {

    private Long id;
    private String titre;
    private String description;
    private TypeContrat typeContrat;
    private String ville;
    private Double salaire;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private StatutOffre statut;
    private Long recruteurId;
    private String recruteurEmail;

    public static OffreResponse from(Offre offre) {
        OffreResponse response = new OffreResponse();
        response.setId(offre.getId());
        response.setTitre(offre.getTitre());
        response.setDescription(offre.getDescription());
        response.setTypeContrat(offre.getTypeContrat());
        response.setVille(offre.getVille());
        response.setSalaire(offre.getSalaire());
        response.setDateCreation(offre.getDateCreation());
        response.setDateExpiration(offre.getDateExpiration());
        response.setStatut(offre.getStatut());
        response.setRecruteurId(offre.getRecruteurId());
        response.setRecruteurEmail(offre.getRecruteurEmail());
        return response;
    }
}