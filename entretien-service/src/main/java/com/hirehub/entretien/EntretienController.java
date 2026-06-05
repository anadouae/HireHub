package com.hirehub.entretien;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.InterviewStatus;
import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.dtos.EntretienAdminStats;
import com.hirehub.entretien.dtos.EntretienResponse;
import com.hirehub.entretien.entities.EntretienType;
import com.hirehub.entretien.services.EntretienService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/entretiens")
@Slf4j
public class EntretienController {

    private final EntretienService entretienService;

    public EntretienController(EntretienService entretienService) {
        this.entretienService = entretienService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EntretienResponse>> create(
            @RequestBody CreateEntretienRequest request) {
        try {
            EntretienResponse response = EntretienResponse.from(entretienService.create(request));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Entretien planifie", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Requête invalide — vérifiez le type (PRESENTIEL, VISIO, TELEPHONIQUE), la date et le lieu/lien."));
        } catch (Exception e) {
            log.error("Erreur création entretien", e);
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la planification : " + detail));
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<EntretienResponse>>> listAdmin(
            @RequestParam(required = false) InterviewStatus status,
            @RequestParam(required = false) EntretienType type,
            @RequestParam(required = false) String recruteurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        Page<EntretienResponse> page = entretienService.listAdmin(status, type, recruteurId, from, to, pageable)
                .map(EntretienResponse::from);
        return ResponseEntity.ok(ApiResponse.ok("Entretiens admin", page));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<EntretienAdminStats>> adminStats() {
        return ResponseEntity.ok(ApiResponse.ok("Statistiques entretiens", entretienService.adminStats()));
    }

    @GetMapping("/candidature/{candidatureId}")
    public ResponseEntity<ApiResponse<List<EntretienResponse>>> listByCandidature(
            @PathVariable String candidatureId) {
        List<EntretienResponse> list = entretienService.listByCandidature(candidatureId)
                .stream().map(EntretienResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Entretiens recuperes", list));
    }

    @GetMapping("/recruteur/{recruteurId}")
    public ResponseEntity<ApiResponse<List<EntretienResponse>>> listByRecruteur(
            @PathVariable String recruteurId) {
        List<EntretienResponse> list = entretienService.listByRecruteur(recruteurId)
                .stream().map(EntretienResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Entretiens recuperes", list));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<ApiResponse<List<EntretienResponse>>> listByCandidat(
            @PathVariable String candidatId) {
        List<EntretienResponse> list = entretienService.listByCandidat(candidatId)
                .stream().map(EntretienResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok("Entretiens recuperes", list));
    }

    @DeleteMapping("/{entretienId}")
    public ResponseEntity<ApiResponse<EntretienResponse>> cancel(
            @PathVariable String entretienId,
            @RequestParam(required = false) String recruteurId,
            @RequestHeader(value = "X-Recruteur-Id", required = false) String recruteurIdHeader) {
        String effectiveRecruteurId = recruteurId != null ? recruteurId : recruteurIdHeader;
        try {
            EntretienResponse response = EntretienResponse.from(
                    entretienService.cancel(entretienId, effectiveRecruteurId));
            return ResponseEntity.ok(ApiResponse.ok("Entretien annule", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        }
    }
}