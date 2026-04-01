package com.hirehub.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureCreatedEvent {
    private String candidatureId;
    private String candidatId;
    private String offreId;
    private String offreTitre;

    // L'email est dans le payload — pas besoin d'appeler auth-service
    private String candidatEmail;
    private String candidatNom;
    private LocalDateTime dateSoumission;
}
