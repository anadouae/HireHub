package com.hirehub.entretien.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hirehub.entretien.entities.EntretienType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class CreateEntretienRequest {
    private String candidatureId;
    private String recruteurId;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;
    private EntretienType type;
    private String notesInternes;

    @JsonProperty("type")
    public void setTypeFromJson(String typeValue) {
        if (typeValue == null || typeValue.isBlank()) {
            this.type = null;
            return;
        }
        this.type = EntretienType.valueOf(typeValue.trim().toUpperCase());
    }
}