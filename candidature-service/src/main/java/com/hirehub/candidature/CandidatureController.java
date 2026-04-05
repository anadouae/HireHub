package com.hirehub.candidature;

import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.services.CandidatureService;
import com.hirehub.common.dtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les candidatures
 * Endpoints: /candidatures
 */
@RestController
@RequestMapping("/candidatures")
@Slf4j
public class CandidatureController {

    private final CandidatureService candidatureService;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    /**
     * Crée une nouvelle candidature
     * POST /candidatures
     *
     * Un candidat ne peut postuler qu'une seule fois par offre
     * L'offre doit être ouverte au moment de la candidature
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Candidature>> createCandidature(
            @RequestBody Candidature candidature
    ) {
        log.info("Création d'une candidature pour l'offre: {}", candidature.getOffreId());
        try {
            candidatureService.createCandidatureByCandidat(candidature);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Candidature créée avec succès", candidature));
        } catch (Exception e) {
            log.error("Erreur lors de la création: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Récupère les candidatures du candidat connecté
     * GET /candidatures/moi
     */
    @GetMapping("/moi")
    public ResponseEntity<ApiResponse<List<Candidature>>> getMyCandidatures() {
        log.info("Récupération des candidatures du candidat");
        try {
            List<Candidature> candidatures = candidatureService.getMyCandidaturesByCandidat();
            return ResponseEntity.ok(ApiResponse.ok("Candidatures récupérées", candidatures));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Récupère les candidatures pour une offre (Vue Recruteur)
     * GET /candidatures/offre/{offreId}
     *
     * Vérifie que l'utilisateur est recruteur
     */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<ApiResponse<List<Candidature>>> getCandidaturesByOffre(
            @PathVariable String offreId
    ) {
        log.info("Récupération des candidatures pour l'offre: {}", offreId);
        try {
            List<Candidature> candidatures = candidatureService.getCandidaturesByOfferIdByRecruiter(offreId);
            return ResponseEntity.ok(ApiResponse.ok("Candidatures pour l'offre récupérées", candidatures));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Récupère une candidature par son ID
     * GET /candidatures/{candidatureId}
     */
    @GetMapping("/{candidatureId}")
    public ResponseEntity<ApiResponse<Candidature>> getCandidatureById(
            @PathVariable String candidatureId
    ) {
        log.info("Récupération de la candidature: {}", candidatureId);
        try {
            Candidature candidature = candidatureService.getCandidatureById(candidatureId);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }
            return ResponseEntity.ok(ApiResponse.ok("Candidature récupérée", candidature));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Met à jour le statut d'une candidature (Action Recruteur)
     * PUT /candidatures/{candidatureId}/status
     *
     * Vérifie que l'utilisateur est recruteur
     * Met à jour le statut et ajoute un historique
     */
    @PutMapping("/{candidatureId}/status")
    public ResponseEntity<ApiResponse<Candidature>> updateCandidatureStatus(
            @PathVariable String candidatureId,
            @RequestParam String newStatus
    ) {
        log.info("Mise à jour du statut de la candidature: {} -> {}", candidatureId, newStatus);
        try {
            candidatureService.updateCandidatureStatusByRecruiter(candidatureId, newStatus);
            Candidature updated = candidatureService.getCandidatureById(candidatureId);
            return ResponseEntity.ok(ApiResponse.ok("Statut mis à jour", updated));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Met à jour les détails d'une candidature (CV et lettre de motivation)
     * PATCH /candidatures/{candidatureId}
     *
     * Vérifie que l'utilisateur est le candidat propriétaire
     */
    @PatchMapping("/{candidatureId}")
    public ResponseEntity<ApiResponse<Candidature>> patchCandidatureDetails(
            @PathVariable String candidatureId,
            @RequestParam(required = false) String CV_Path,
            @RequestParam(required = false) String lettreMotivationPath
    ) {
        log.info("Mise à jour des détails de la candidature: {}", candidatureId);
        try {
            candidatureService.updateCandidatureDetailsByCandidat(candidatureId, CV_Path, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(candidatureId);
            // garder les fichiers dans le volume docker
            return ResponseEntity.ok(ApiResponse.ok("Détails mis à jour", updated));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Récupère l'historique des changements de statut d'une candidature
     * GET /candidatures/{candidatureId}/historique
     *
     * Vérifie que l'utilisateur est le candidat propriétaire ou le recruteur
     */
    @GetMapping("/{candidatureId}/historique")
    public ResponseEntity<ApiResponse<List<HistoriqueStatus>>> getCandidatureHistorique(
            @PathVariable String candidatureId
    ) {
        log.info("Récupération de l'historique de la candidature: {}", candidatureId);
        // verifie que l'utilisateur est le candidat propriétaire ou le recruteur de l'offre associée
        try {
            Candidature candidature = candidatureService.getCandidatureById(candidatureId);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }
            // TODO: Implémenter la récupération de l'historique
            return ResponseEntity.ok(ApiResponse.ok("Historique récupéré", List.of()));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Upload du CV et de la lettre de motivation
     * POST /candidatures/{candidatureId}/cv-cl
     *
     * Vérifie que l'utilisateur est le candidat propriétaire
     * Enregistre les fichiers dans un volume/stockage
     */
    @PostMapping("/{candidatureId}/cv-cl")
    public ResponseEntity<ApiResponse<Candidature>> uploadCVAndCoverLetter(
            @PathVariable String candidatureId,
            @RequestParam String CV_Path,
            @RequestParam String lettreMotivationPath
    ) {
        log.info("Upload des fichiers pour la candidature: {}", candidatureId);
        try {
            candidatureService.uploadCVAndCoverLetter(candidatureId, CV_Path, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(candidatureId);
            return ResponseEntity.ok(ApiResponse.ok("Fichiers uploadés", updated));
        } catch (Exception e) {
            log.error("Erreur lors de l'upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Supprime une candidature (Action Candidat)
     * DELETE /candidatures/{candidatureId}
     */
    @DeleteMapping("/{candidatureId}")
    public ResponseEntity<ApiResponse<Void>> deleteCandidature(
            @PathVariable String candidatureId
    ) {
        log.info("Suppression de la candidature: {}", candidatureId);
        try {
            candidatureService.deleteCandidatureByCandidat(candidatureId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
