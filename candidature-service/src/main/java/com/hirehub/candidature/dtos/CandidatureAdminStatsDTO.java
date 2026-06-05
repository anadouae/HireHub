package com.hirehub.candidature.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class CandidatureAdminStatsDTO {
    private final long total;
    private final Map<String, Long> byStatus;
}
