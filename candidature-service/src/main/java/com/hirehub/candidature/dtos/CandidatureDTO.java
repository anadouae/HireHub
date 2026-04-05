package com.hirehub.candidature.dtos;

import com.hirehub.common.enums.CandidatureStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO pour les candidatures dans les réponses API
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureDTO {
    private String id;
    private String candidatId;
    private String offreId;
    private String CV_Path;
    private String lettreMotivationPath;
    private CandidatureStatus status;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;
}

