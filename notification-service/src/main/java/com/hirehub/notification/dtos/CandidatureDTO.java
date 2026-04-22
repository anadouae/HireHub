package com.hirehub.notification.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidatureDTO {
    @NotBlank
    String candidatEmail;
    @NotBlank
    String candidatName;
    @NotBlank
    String offreTitle;
    @Min(1) @NotNull
    Long offreId;
}
