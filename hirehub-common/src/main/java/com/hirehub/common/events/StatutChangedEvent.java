package com.hirehub.common.events;

import com.hirehub.common.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatutChangedEvent {
    private String candidatureId;
    private String candidatId;
    private String candidatEmail;   // idem — pas besoin de Feign
    private String candidatNom;
    private String offreTitre;
    private ApplicationStatus ancienStatut;
    private ApplicationStatus nouveauStatut;
    private LocalDateTime dateChangement;
}
