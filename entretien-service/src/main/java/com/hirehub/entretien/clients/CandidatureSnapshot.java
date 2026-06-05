package com.hirehub.entretien.clients;

import com.hirehub.common.enums.CandidatureStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CandidatureSnapshot {
    private String id;
    private String candidatId;
    private String candidatEmail;
    private String offreId;
    private CandidatureStatus status;
}