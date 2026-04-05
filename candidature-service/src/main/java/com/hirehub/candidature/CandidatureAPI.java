package com.hirehub.candidature;

import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.common.dtos.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Interface API pour les candidatures
 * Définit les contrats pour les endpoints REST
 */
@Tag(name = "Candidatures", description = "Gestion des candidatures")
public interface CandidatureAPI {

    /**
     * Crée une nouvelle candidature
     */
    ApiResponse<Candidature> createCandidature(Candidature candidature);

    /**
     * Récupère les candidatures du candidat
     */
    ApiResponse<List<Candidature>> getMyCandidatures();

    /**
     * Récupère les candidatures pour une offre
     */
    ApiResponse<List<Candidature>> getCandidaturesByOffre(String offreId);

    /**
     * Récupère une candidature par ID
     */
    ApiResponse<Candidature> getCandidatureById(String candidatureId);

    /**
     * Met à jour le statut
     */
    ApiResponse<Candidature> updateStatus(String candidatureId, String newStatus);

    /**
     * Met à jour les fichiers
     */
    ApiResponse<Candidature> updateFiles(String candidatureId, String CV_Path, String lettreMotivationPath);

    /**
     * Récupère l'historique
     */
    ApiResponse<List<HistoriqueStatus>> getHistorique(String candidatureId);

    /**
     * Upload des fichiers
     */
    ApiResponse<Candidature> uploadFiles(String candidatureId, String CV_Path, String lettreMotivationPath);

    /**
     * Supprime une candidature
     */
    ApiResponse<Void> deleteCandidature(String candidatureId);
}
