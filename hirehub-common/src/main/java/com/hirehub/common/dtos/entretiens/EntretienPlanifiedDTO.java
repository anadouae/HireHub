package com.hirehub.common.dtos.entretiens;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntretienPlanifiedDTO {
    private String candidatEmail;
    private String candidatName;
    private String offreTitle;
    private String heureEntretien;
    private String candidatureId;
    private String commentaire;
    private String dateEntretien;
    private String lieuEntretien;
    private String interviewerName;
}
