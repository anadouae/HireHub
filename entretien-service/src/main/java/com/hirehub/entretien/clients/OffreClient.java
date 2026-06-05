package com.hirehub.entretien.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "offre-service", path = "/api/offres")
public interface OffreClient {

    @GetMapping("/{id}")
    OffreTitleSnapshot getOffre(@PathVariable("id") Long id);
}
