package com.hirehub.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CandidatureStatutChangedDTO {
    Long candidatureId;
    String candidatEmail;
    String candidatName;
    String offreTitle;
    String ancienStatut;
    String nouveauStatut;
    String commentaire;
}
