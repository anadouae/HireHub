package com.hirehub.event.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CandidatureChangedDTO {
    Long candidatureId;
    String candidatEmail;
    String candidatName;
    String offreTitle;
    String ancienStatut;
    String nouveauStatut;
    String commentaire;
}
