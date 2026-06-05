package com.hirehub.candidature.web;

import com.hirehub.candidature.clients.ICandidatureAPI;
import com.hirehub.candidature.config.CandidatureSecurityService;
import com.hirehub.candidature.config.RequireAuth;
import com.hirehub.candidature.config.UserContext;
import com.hirehub.candidature.dtos.CandidatureAdminStatsDTO;
import com.hirehub.candidature.dtos.CandidatureCreatedDTO;
import com.hirehub.candidature.dtos.CandidatureResponseDTO;
import com.hirehub.candidature.dtos.HistoriqueStatusDTO;
import com.hirehub.candidature.repository.CandidatureRepository;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.exceptions.CandidatureChangedStatusException;
import com.hirehub.candidature.exceptions.UnauthorizedException;
import com.hirehub.candidature.mapper.CandidatureMapper;
import com.hirehub.candidature.mapper.HistoriqueStatusMapper;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.candidature.services.ICandidatureService;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.UserRole;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur REST pour les candidatures.
 * Tous les endpoints sont protégés via {@link RequireAuth} et {@link CandidatureSecurityService}.
 */
@RestController
@RequestMapping("/candidatures")
@Slf4j
@RequireAuth
public class CandidatureController implements ICandidatureAPI {

    private final ICandidatureService candidatureService;
    private final HistoriqueStatusRepository historiqueStatusRepository;
    private final CandidatureRepository candidatureRepository;
    private final CandidatureSecurityService securityService;
    private final CandidatureMapper candidatureMapper;
    private final HistoriqueStatusMapper historiqueStatusMapper;

    public CandidatureController(ICandidatureService candidatureService,
                                 HistoriqueStatusRepository historiqueStatusRepository,
                                 CandidatureRepository candidatureRepository,
                                 CandidatureSecurityService securityService,
                                 CandidatureMapper candidatureMapper,
                                 HistoriqueStatusMapper historiqueStatusMapper) {
        this.candidatureService = candidatureService;
        this.historiqueStatusRepository = historiqueStatusRepository;
        this.candidatureRepository = candidatureRepository;
        this.securityService = securityService;
        this.candidatureMapper = candidatureMapper;
        this.historiqueStatusMapper = historiqueStatusMapper;
    }

    /**
     * POST /candidatures
     */
    @PostMapping
    @RequireAuth
    @Override
    public ResponseEntity<ApiResponse<CandidatureResponseDTO>> create(
            @Valid @RequestBody CandidatureCreatedDTO candidatureDTO) {

        Candidature candidature = CandidatureMapper.toEntity(candidatureDTO);

        Candidature candidatureCreated = candidatureService.createCandidatureByCandidat(candidature);

        CandidatureResponseDTO candidatureResponseDTO = candidatureMapper.toResponseDTO(candidatureCreated);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Candidature créée", candidatureResponseDTO));
    }

    /**
     * GET /candidatures/moi
     */
    @GetMapping("/moi")
    public ResponseEntity<ApiResponse<List<CandidatureResponseDTO>>> myCandidatures() {
        securityService.requireAuth();
        List<Candidature> data = candidatureService.getMyCandidaturesByCandidat();

        List<CandidatureResponseDTO> candidatureResponseDTO = candidatureMapper.toResponseDtos(data);

        return ResponseEntity.ok(ApiResponse.ok(
                "Candidatures récupérées", candidatureResponseDTO));

    }

    /**
     * GET /candidatures/{id}
     */
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponse<CandidatureResponseDTO>> get(@PathVariable String id) {

        Candidature candidature = candidatureService.getCandidatureById(id);
        if (candidature == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Candidature non trouvée"));
        }

        CandidatureResponseDTO candidatureResponseDTO = candidatureMapper.toResponseDTO(candidature);

        return ResponseEntity.ok(ApiResponse.ok("Candidature récupérée", candidatureResponseDTO));
    }

    /**
     * PUT /candidatures/{candidatureId}/status?status=ACCEPTEE
     */
    @PutMapping("/{candidatureId}/status")
    @Override
    public ResponseEntity<ApiResponse<CandidatureResponseDTO>> updateStatus(@PathVariable String candidatureId,
                                                                 @RequestParam("status") String newStatus) {
        try {
            // Vérifier l'authentification et les droits
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(candidatureId);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le recruteur peut changer le statut
            securityService.requireRecruteurCanChangeStatus(user, candidature);

            // Mettre à jour le statut
            candidatureService.updateCandidatureStatusByRecruiter(candidatureId, newStatus);
            Candidature updated = candidatureService.getCandidatureById(candidatureId);

            CandidatureResponseDTO candidatureResponseDTO = candidatureMapper.toResponseDTO(updated);

            return ResponseEntity.ok(ApiResponse.ok("Statut mis à jour", candidatureResponseDTO));

        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
        catch (CandidatureChangedStatusException e) {
            log.warn("Statut invalide: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
        catch (Exception e) {
            log.error("Erreur update statut candidature {}: {}", candidatureId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PATCH /candidatures/{id}?cvPath=...&lettreMotivationPath=...
     */
    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<ApiResponse<CandidatureResponseDTO>> updateFiles(@PathVariable String id,
                                                                @RequestParam(value = "cvPath", required = false) String cvPath,
                                                                @RequestParam(value = "lettreMotivationPath", required = false) String lettreMotivationPath) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le candidat peut mettre à jour ses fichiers
            securityService.requireCandidatCanUpdateFiles(user, candidature);

            candidatureService.updateCandidatureDetailsByCandidat(id, cvPath, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(id);
            // Les fichiers sont maintenant stockés dans /app/uploads/ (volume Docker)
            // Cette logique est gérée dans le service ICandidatureServiceImpl.uploadCVAndCoverLetter()
            CandidatureResponseDTO candidatureResponseDTO = candidatureMapper.toResponseDTO(updated);

            return ResponseEntity.ok(ApiResponse.ok("Fichiers mis à jour", candidatureResponseDTO));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur update fichiers candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Override
    public ApiResponse<Candidature> uploadFiles(String candidatureId, String CV_Path, String lettreMotivationPath) {
        candidatureService.uploadCVAndCoverLetter(candidatureId, CV_Path, lettreMotivationPath);
        Candidature updated = candidatureService.getCandidatureById(candidatureId);
        return ApiResponse.ok("Fichiers uploadés", updated);
    }

    /**
     * POST /candidatures/{id}/upload?cvPath=...&lettreMotivationPath=...
     *
     * Ici, on garde l'API actuelle (paths) en attendant une vraie intégration Multipart.
     */
    @PostMapping("/{id}/upload")
    public ResponseEntity<ApiResponse<CandidatureResponseDTO>> upload(@PathVariable String id,
                                                           @RequestParam("cvPath") String cvPath,
                                                           @RequestParam("lettreMotivationPath") String lettreMotivationPath) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le candidat peut mettre à jour ses fichiers
            securityService.requireCandidatCanUpdateFiles(user, candidature);

            candidatureService.uploadCVAndCoverLetter(id, cvPath, lettreMotivationPath);
            Candidature updated = candidatureService.getCandidatureById(id);

            CandidatureResponseDTO candidatureResponseDTO = candidatureMapper.toResponseDTO(updated);

            return ResponseEntity.ok(ApiResponse.ok("Upload enregistré", candidatureResponseDTO));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur upload fichiers candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /candidatures/{id}/historique
     */
    @GetMapping("/{candidatureId}/historique")
    @Override
    public ResponseEntity<ApiResponse<List<HistoriqueStatusDTO>>> getHistorique(@PathVariable String candidatureId)
            throws Exception {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(candidatureId);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que (candidat propriétaire) OU (recruteur propriétaire de l'offre)
            if (UserRole.CANDIDAT.name().equals(user.role)) {
                securityService.requireCandidatCanViewHistory(user, candidature);
            } else if (UserRole.RECRUTEUR.name().equals(user.role)) {
                securityService.requireRecruteurCanViewHistory(user, candidature);
            } else {
                throw new UnauthorizedException("Rôle utilisateur non reconnu");
            }

            List<HistoriqueStatus> data = historiqueStatusRepository.findByCandidatureIdOrderByDateChangementDesc(candidatureId);

            return ResponseEntity.ok(ApiResponse.ok(
                    "Historique récupéré",
                    historiqueStatusMapper.toResponseDtos(data)));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé à l'historique: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur récupération historique candidature {}: {}", candidatureId, e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * POST /candidatures/offres/{offreId}/reject-pending-on-close
     * Refuse les candidatures SOUMISE lors de la fermeture d'une offre.
     */
    @PostMapping("/offres/{offreId}/reject-pending-on-close")
    public ResponseEntity<ApiResponse<java.util.Map<String, Integer>>> rejectPendingOnOfferClose(
            @PathVariable String offreId) {
        try {
            securityService.requireAuth();
            int rejected = candidatureService.rejectPendingWhenOfferClosed(offreId);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Candidatures en attente refusées",
                    java.util.Map.of("rejectedCount", rejected)));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur fermeture offre {} : {}", offreId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/admin/offre/{offreId}/count")
    public ResponseEntity<ApiResponse<Long>> countByOffreAdmin(@PathVariable String offreId) {
        try {
            securityService.requireAuth();
            return ResponseEntity.ok(ApiResponse.ok("Nombre de candidatures",
                    candidatureRepository.countByOffreId(offreId)));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<CandidatureAdminStatsDTO>> adminStats() {
        try {
            securityService.requireAuth();
            Map<String, Long> byStatus = new HashMap<>();
            for (CandidatureStatus status : CandidatureStatus.values()) {
                byStatus.put(status.name(), candidatureRepository.countByStatus(status));
            }
            CandidatureAdminStatsDTO stats = new CandidatureAdminStatsDTO(
                    candidatureRepository.count(),
                    byStatus
            );
            return ResponseEntity.ok(ApiResponse.ok("Statistiques candidatures", stats));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /candidatures/offre/{offreId}
     * Pipeline du recruteur: liste de toutes les candidatures pour une offre
     */
    @GetMapping("/offre/{offreId}")
    @Override
    public ResponseEntity<ApiResponse<List<CandidatureResponseDTO>>> getCandidaturesByOffreForRecruteur(@PathVariable String offreId) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            securityService.requireAdminOrRecruteurPipeline(user, offreId);

            List<Candidature> data = candidatureService.getCandidaturesByOfferIdByRecruiter(offreId);

            List<CandidatureResponseDTO> candidatureResponseDTO = candidatureMapper.toResponseDtos(data);
            return ResponseEntity.ok(ApiResponse.ok("Candidatures récupérées", candidatureResponseDTO));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur récupération candidatures offre {}: {}", offreId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /candidatures/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Candidature non trouvée"));
            }

            // Vérifier que le candidat peut supprimer sa candidature
            securityService.requireCandidatCanDeleteCandidature(user, candidature);

            candidatureService.deleteCandidatureByCandidat(id);
            return ResponseEntity.ok(ApiResponse.ok("Candidature supprimée", null));
        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur suppression candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /candidatures/{id}/download?type=cv|lettre
     *
     * Télécharge les fichiers CV ou lettre de motivation de manière sécurisée.
     * Vérifie que l'utilisateur est :
     * - Le candidat propriétaire (pour son propre CV)
     * - Un recruteur autorisé (pour voir les candidatures)
     */
    @GetMapping("/file/{id}/download")
    @Override
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String id,
            @RequestParam(value = "type", defaultValue = "cv") String fileType) {

        try {
            UserContext.UserInfo user = securityService.requireAuth();

            Candidature candidature = candidatureService.getCandidatureById(id);
            if (candidature == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Vérification des droits d'accès selon le rôle
            if (UserRole.CANDIDAT.name().equals(user.role)) {
                securityService.requireCandidatCanDownloadOwnFile(user, candidature);
            } else if (UserRole.RECRUTEUR.name().equals(user.role)) {
                securityService.requireRecruteurCanDownloadFile(user, candidature);
            } else {
                throw new UnauthorizedException("Rôle utilisateur non reconnu");
            }

            // Obtenir le chemin du fichier
            String filePath = fileType.equalsIgnoreCase("cv")
                ? candidature.getCvPath()
                : candidature.getLettreMotivationPath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Créer le chemin absolu et normalisé (résout les segments ".." avant toute vérification)
            Path uploadsRoot = Paths.get("/app/uploads/");
            Path fileAbsolutePath = uploadsRoot.resolve(filePath).normalize();

            // Rejet si le chemin normalisé sort du répertoire uploads (path traversal)
            if (!fileAbsolutePath.startsWith(uploadsRoot)) {
                log.warn("Tentative de path traversal rejetée: {}", fileAbsolutePath);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            Resource resource = new FileSystemResource(fileAbsolutePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.error("Fichier non trouvé ou non lisible: {}", fileAbsolutePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Déterminer le type MIME du fichier
            String contentType = Files.probeContentType(fileAbsolutePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Créer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));

            log.info("Streaming fichier {} pour candidature {}", filePath, id);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (UnauthorizedException e) {
            log.warn("Accès non autorisé au téléchargement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Erreur téléchargement fichier candidature {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
