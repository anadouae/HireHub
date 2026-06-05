package com.hirehub.frontend.candidature;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.offre.RecruiterContext;
import com.hirehub.frontend.auth.SessionAuthSupport;
import com.hirehub.frontend.clients.CandidatureDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CandidatureFrontendClient {

    private static final Logger log = LoggerFactory.getLogger(CandidatureFrontendClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String candidatureBaseUrl;

    public CandidatureFrontendClient(
            @Value("${hirehub.candidature-service-base-url}") String candidatureBaseUrl
    ) {
        this.candidatureBaseUrl = candidatureBaseUrl;
    }

    public List<CandidatureDTO> getMyCandidatures() {
        return fetchList(candidatureBaseUrl + "/candidatures/moi");
    }

    public List<CandidatureDTO> getCandidaturesByOffre(String offreId) {
        RecruiterContext.requireRecruiter();
        return fetchList(candidatureBaseUrl + "/candidatures/offre/" + offreId);
    }

    public List<CandidatureDTO> getCandidaturesByOffreAdmin(String offreId) {
        return fetchList(candidatureBaseUrl + "/candidatures/offre/" + offreId);
    }

    public long countByOffre(String offreId) {
        try {
            ResponseEntity<ApiResponse<Long>> response = restTemplate.exchange(
                    candidatureBaseUrl + "/candidatures/admin/offre/" + offreId + "/count",
                    HttpMethod.GET,
                    authEntity(null),
                    new ParameterizedTypeReference<ApiResponse<Long>>() {}
            );
            ApiResponse<Long> body = response.getBody();
            return body != null && body.getData() != null ? body.getData() : 0L;
        } catch (RestClientException ex) {
            log.warn("Count candidatures offre {} : {}", offreId, ex.getMessage());
            return 0L;
        }
    }

    public Map<String, Long> adminStats() {
        try {
            ResponseEntity<ApiResponse<CandidatureAdminStatsApi>> response = restTemplate.exchange(
                    candidatureBaseUrl + "/candidatures/admin/stats",
                    HttpMethod.GET,
                    authEntity(null),
                    new ParameterizedTypeReference<ApiResponse<CandidatureAdminStatsApi>>() {}
            );
            ApiResponse<CandidatureAdminStatsApi> body = response.getBody();
            if (body != null && body.getData() != null) {
                Map<String, Long> result = new java.util.HashMap<>(body.getData().getByStatus());
                result.put("total", body.getData().getTotal());
                return result;
            }
        } catch (RestClientException ex) {
            log.warn("Stats candidatures admin: {}", ex.getMessage());
        }
        return Map.of();
    }

    public Optional<CandidatureDTO> getCandidature(String id) {
        try {
            ResponseEntity<ApiResponse<CandidatureApiItem>> response = restTemplate.exchange(
                    candidatureBaseUrl + "/candidatures/" + id,
                    HttpMethod.GET,
                    authEntity(null),
                    new ParameterizedTypeReference<ApiResponse<CandidatureApiItem>>() {}
            );
            ApiResponse<CandidatureApiItem> body = response.getBody();
            if (body != null && body.getData() != null) {
                return Optional.of(toDto(body.getData()));
            }
            return Optional.empty();
        } catch (HttpStatusCodeException ex) {
            log.warn("Candidature {} : HTTP {}", id, ex.getStatusCode());
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Candidature {} : {}", id, ex.getMessage());
            return Optional.empty();
        }
    }

    public CandidatureDTO create(String offreId, String cvPath, String lettrePath, HirehubUserDetails candidat) {
        CandidatureCreateRequest body = new CandidatureCreateRequest(
                UUID.randomUUID().toString(),
                candidat.getId().toString(),
                offreId,
                cvPath,
                lettrePath,
                CandidatureStatus.SOUMISE
        );
        try {
            ResponseEntity<ApiResponse<CandidatureApiItem>> response = restTemplate.exchange(
                    candidatureBaseUrl + "/candidatures",
                    HttpMethod.POST,
                    authEntity(body),
                    new ParameterizedTypeReference<ApiResponse<CandidatureApiItem>>() {}
            );
            ApiResponse<CandidatureApiItem> apiBody = response.getBody();
            if (apiBody != null && apiBody.getData() != null) {
                return toDto(apiBody.getData());
            }
            throw new CandidatureServiceException("Réponse candidature invalide");
        } catch (HttpStatusCodeException ex) {
            log.warn("Création candidature offre {} : {} {}", offreId, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new CandidatureServiceException(parseError(ex), ex);
        } catch (RestClientException ex) {
            throw new CandidatureServiceException("Service candidatures indisponible", ex);
        }
    }

    public int rejectPendingOnOfferClose(String offreId) {
        String url = candidatureBaseUrl + "/candidatures/offres/" + offreId + "/reject-pending-on-close";
        try {
            ResponseEntity<ApiResponse<Map<String, Integer>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    authEntity(null),
                    new ParameterizedTypeReference<ApiResponse<Map<String, Integer>>>() {}
            );
            ApiResponse<Map<String, Integer>> body = response.getBody();
            if (body != null && body.getData() != null && body.getData().get("rejectedCount") != null) {
                return body.getData().get("rejectedCount");
            }
            return 0;
        } catch (HttpStatusCodeException ex) {
            log.warn("Refus candidatures en attente offre {} : {} {}", offreId, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new CandidatureServiceException("Impossible de clôturer les candidatures en attente", ex);
        } catch (RestClientException ex) {
            throw new CandidatureServiceException("Service candidatures indisponible", ex);
        }
    }

    public void updateStatus(String candidatureId, String status) {
        String url = candidatureBaseUrl + "/candidatures/" + candidatureId + "/status?status=" + status;
        try {
            restTemplate.exchange(url, HttpMethod.PUT, authEntity(null), Void.class);
        } catch (HttpStatusCodeException ex) {
            throw new CandidatureServiceException("Changement de statut refusé", ex);
        } catch (RestClientException ex) {
            throw new CandidatureServiceException("Service candidatures indisponible", ex);
        }
    }

    public void delete(String candidatureId) {
        try {
            restTemplate.exchange(
                    candidatureBaseUrl + "/candidatures/" + candidatureId,
                    HttpMethod.DELETE,
                    authEntity(null),
                    Void.class
            );
        } catch (HttpStatusCodeException ex) {
            throw new CandidatureServiceException("Suppression refusée", ex);
        } catch (RestClientException ex) {
            throw new CandidatureServiceException("Service candidatures indisponible", ex);
        }
    }

    public List<HistoriqueApiItem> getHistorique(String candidatureId) {
        try {
            ResponseEntity<ApiResponse<List<HistoriqueApiItem>>> response = restTemplate.exchange(
                    candidatureBaseUrl + "/candidatures/" + candidatureId + "/historique",
                    HttpMethod.GET,
                    authEntity(null),
                    new ParameterizedTypeReference<ApiResponse<List<HistoriqueApiItem>>>() {}
            );
            ApiResponse<List<HistoriqueApiItem>> body = response.getBody();
            if (body == null || body.getData() == null) {
                return Collections.emptyList();
            }
            return body.getData();
        } catch (HttpStatusCodeException ex) {
            log.warn("Historique candidature {} : HTTP {}", candidatureId, ex.getStatusCode());
            throw new CandidatureServiceException("Impossible de charger l'historique", ex);
        } catch (RestClientException ex) {
            throw new CandidatureServiceException("Service candidatures indisponible", ex);
        }
    }

    private List<CandidatureDTO> fetchList(String url) {
        try {
            ResponseEntity<ApiResponse<List<CandidatureApiItem>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(null),
                    new ParameterizedTypeReference<ApiResponse<List<CandidatureApiItem>>>() {}
            );
            ApiResponse<List<CandidatureApiItem>> body = response.getBody();
            if (body == null || body.getData() == null) {
                return Collections.emptyList();
            }
            return body.getData().stream().map(this::toDto).collect(Collectors.toList());
        } catch (HttpStatusCodeException ex) {
            log.warn("Liste candidatures {} : HTTP {} {}", url, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new CandidatureServiceException("Impossible de charger les candidatures", ex);
        } catch (RestClientException ex) {
            log.warn("Liste candidatures {} : {}", url, ex.getMessage());
            throw new CandidatureServiceException("Service candidatures indisponible", ex);
        }
    }

    private HttpEntity<?> authEntity(Object body) {
        String token = SessionAuthSupport.accessToken()
                .orElseThrow(() -> new CandidatureServiceException("Session expirée — reconnectez-vous"));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new HttpEntity<>(body, headers);
        }
        return new HttpEntity<>(headers);
    }

    private CandidatureDTO toDto(CandidatureApiItem item) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId(item.getId());
        dto.setOffreId(item.getOffreId());
        dto.setCandidatId(item.getCandidatId());
        dto.setStatus(item.getStatus());
        dto.setCvPath(item.getCvPath());
        dto.setLettreMotivationPath(item.getLettreMotivationPath());
        dto.setDateSoumission(parseDateTime(item.getDateSoumission(), item.getCreatedAt()));
        dto.setDateModification(parseDateTime(item.getDateModification(), null));
        return dto;
    }

    private static LocalDateTime parseDateTime(String primary, String fallback) {
        for (String raw : new String[]{primary, fallback}) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                return LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDateTime.parse(raw + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException ignored2) {
                    // try next
                }
            }
        }
        return null;
    }

    private String parseError(HttpStatusCodeException ex) {
        String raw = ex.getResponseBodyAsString();
        if (raw != null && raw.contains("deja postule")) {
            return "Vous avez déjà postulé à cette offre.";
        }
        if (raw != null && (raw.contains("n'existe pas") || raw.contains("non publie"))) {
            return "Cette offre n'est pas disponible (non publiée ou introuvable).";
        }
        if (raw != null && (raw.contains("Transition invalide") || raw.contains("transition"))) {
            return "Transition de statut non autorisée — suivez : Soumise → En cours → Entretien → Acceptée/Refusée.";
        }
        if (raw != null && raw.contains("Session expir")) {
            return "Session expirée — reconnectez-vous puis réessayez.";
        }
        if (raw != null && raw.contains("\"message\"")) {
            int start = raw.indexOf("\"message\"");
            int colon = raw.indexOf(':', start);
            int firstQuote = raw.indexOf('"', colon + 1);
            int secondQuote = raw.indexOf('"', firstQuote + 1);
            if (firstQuote >= 0 && secondQuote > firstQuote) {
                return raw.substring(firstQuote + 1, secondQuote);
            }
        }
        if (ex.getStatusCode().value() == 403) {
            return "Accès refusé — reconnectez-vous en tant que candidat.";
        }
        if (ex.getStatusCode().value() == 503 || ex.getStatusCode().value() == 502) {
            return "Service candidatures indisponible — vérifiez que la gateway (8089) et candidature-service (8083) sont démarrés.";
        }
        return "Impossible d'enregistrer la candidature (" + ex.getStatusCode().value() + ").";
    }
}
