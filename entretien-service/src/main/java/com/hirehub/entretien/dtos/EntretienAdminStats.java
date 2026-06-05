package com.hirehub.entretien.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntretienAdminStats {
    private final long total;
    private final long planifies;
    private final long annules;
}
