package com.hirehub.candidature.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client Feign pour communiquer avec offre-service
 * Vérifie que les offres existent et sont publiées
 */
@FeignClient(name = "offre-service")
public interface IOffreServiceClient {

    /**
     * Récupère une offre par son ID
     *
     * @param id l'ID de l'offre
     * @return les détails de l'offre
     */
    @GetMapping("/api/offres/{id}")
    OffreDTO getOffre(@PathVariable("id") String id);

    /**
     * Vérifie si une offre existe et est publiée
     *
     * @param id l'ID de l'offre
     * @return true si l'offre existe et est publiée, false sinon
     */
    @GetMapping("/api/offres/{id}/valide")
    boolean offreExists(@PathVariable("id") String id);

    /**
     * Vérifie si un recruteur est propriétaire d'une offre
     *
     * @param offreId l'ID de l'offre
     * @param recruteurId l'ID du recruteur
     * @return true si le recruteur est propriétaire, false sinon
     */
    @GetMapping("/api/offres/{offreId}/owner")
    boolean isRecruteurOwner(
        @PathVariable("offreId") String offreId,
        @RequestParam("recruteurId") String recruteurId
    );

}

