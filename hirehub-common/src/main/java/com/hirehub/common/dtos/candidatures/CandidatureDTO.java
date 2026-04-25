package com.hirehub.common.dtos.candidatures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CandidatureDTO {
    private String candidatEmail;
    private String candidatName;
    private String offreTitle;
    private Long offreId;

}
