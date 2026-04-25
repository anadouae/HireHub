package com.hirehub.event.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntretienPlanifiedDTO {
    private String candidatEmail;
    private String candidatName;
    private String  offreTitle;
    private String dateEntretien;
    private String lieu;
    private String interviewerName;
}
