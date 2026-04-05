package com.hirehub.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntretienPlanifiedEvent {
    private String entretienId;
    private String candidatureId;
    private String candidatId;
    private String candidatEmail;
    private String candidatNom;
    private String offreTitre;
    private LocalDateTime dateHeure;
    private String lieu;
    private String lienVisio;       // nullable si présentiel
    private boolean annule;         // true si c'est un événement d'annulation
}
