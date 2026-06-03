package com.hirehub.candidature.clients;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO pour les offres d'emploi (depuis offre-service).
 * offre-service renvoie {@code statut: "PUBLIEE"} — pas un champ {@code published}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OffreDTO {

    private String id;
    private String titre;
    private String description;
    private String recruteurId;
  /** Valeurs attendues : BROUILLON, PUBLIEE, FERMEE (OffreResponse.statut). */
    private String statut;
    private long createdAt;
    private long updatedAt;

    @JsonProperty("id")
    public void setIdFromJson(Object id) {
        this.id = id == null ? null : String.valueOf(id);
    }

    @JsonIgnore
    public boolean isPublished() {
        return "PUBLIEE".equalsIgnoreCase(statut);
    }

    /** Utilisé par FakeOffreServiceClient (profil sandbox). */
    public static OffreDTO sandbox(
            String id,
            String titre,
            String description,
            String recruteurId,
            boolean published
    ) {
        OffreDTO dto = new OffreDTO();
        dto.setId(id);
        dto.setTitre(titre);
        dto.setDescription(description);
        dto.setRecruteurId(recruteurId);
        dto.setStatut(published ? "PUBLIEE" : "BROUILLON");
        dto.setCreatedAt(System.currentTimeMillis());
        dto.setUpdatedAt(dto.getCreatedAt());
        return dto;
    }
}
