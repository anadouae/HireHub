package com.hirehub.entretien.clients;

import com.hirehub.entretien.config.EntretienFeignClientConfig;
import com.hirehub.common.dtos.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "candidature-service", path = "/candidatures", configuration = EntretienFeignClientConfig.class)
public interface CandidatureClient {

    @GetMapping("/{candidatureId}")
    ApiResponse<CandidatureSnapshot> getCandidatureById(@PathVariable String candidatureId);

    @PutMapping("/{candidatureId}/status")
    ApiResponse<CandidatureSnapshot> updateStatus(
            @PathVariable String candidatureId,
            @RequestParam("status") String newStatus
    );
}