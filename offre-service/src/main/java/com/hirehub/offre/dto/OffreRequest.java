package com.hirehub.offre.dto;

import com.hirehub.offre.enums.TypeContrat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OffreRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "Le type de contrat est obligatoire")
    private TypeContrat typeContrat;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @PositiveOrZero(message = "Le salaire doit etre positif ou nul")
    private Double salaire;

    @Future(message = "La date d'expiration doit etre dans le futur")
    private LocalDateTime dateExpiration;
}
