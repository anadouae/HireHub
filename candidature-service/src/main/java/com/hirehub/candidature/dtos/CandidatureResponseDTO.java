package com.hirehub.candidature.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidatureResponseDTO {
    private String id;
    private String offreId;
    private String candidatId;
    private String status;
    private String message;
    private String createdAt;
    private String dateSoumission;
    private String dateModification;
    private String cvPath;
    private String lettreMotivationPath;
}
