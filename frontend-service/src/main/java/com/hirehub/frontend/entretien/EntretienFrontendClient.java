package com.hirehub.frontend.entretien;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.auth.SessionAuthSupport;
import com.hirehub.frontend.offre.RecruiterContext;
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

import java.util.Collections;
import java.util.List;

@Service
public class EntretienFrontendClient {

    private static final Logger log = LoggerFactory.getLogger(EntretienFrontendClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String entretienBaseUrl;

    public EntretienFrontendClient(@Value("${hirehub.entretien-service-base-url}") String entretienBaseUrl) {
        this.entretienBaseUrl = entretienBaseUrl;
    }

    public List<EntretienView> listForRecruiter() {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        return listByRecruteur(recruiter.getId().toString());
    }

    public List<EntretienView> listForCandidat(HirehubUserDetails candidat) {
        return listByCandidat(candidat.getId().toString());
    }

    public java.util.Map<String, Long> adminStats() {
        try {
            ResponseEntity<ApiResponse<EntretienAdminStatsApi>> response = restTemplate.exchange(
                    entretienBaseUrl + "/entretiens/admin/stats",
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<ApiResponse<EntretienAdminStatsApi>>() {}
            );
            ApiResponse<EntretienAdminStatsApi> body = response.getBody();
            if (body != null && body.getData() != null) {
                EntretienAdminStatsApi stats = body.getData();
                return java.util.Map.of(
                        "total", stats.getTotal(),
                        "planifies", stats.getPlanifies(),
                        "annules", stats.getAnnules()
                );
            }
        } catch (RestClientException ex) {
            log.warn("Stats entretiens admin: {}", ex.getMessage());
        }
        return java.util.Map.of();
    }

    public EntretienAdminPageResponse listAdmin(String status, String type, String recruteurId, int page) {
        String url = entretienBaseUrl + "/entretiens/admin?page=" + page + "&size=20&sort=dateHeure,desc"
                + (status != null && !status.isBlank() ? "&status=" + status : "")
                + (type != null && !type.isBlank() ? "&type=" + type : "")
                + (recruteurId != null && !recruteurId.isBlank() ? "&recruteurId=" + recruteurId : "");
        try {
            ResponseEntity<ApiResponse<EntretienAdminPageResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<ApiResponse<EntretienAdminPageResponse>>() {}
            );
            ApiResponse<EntretienAdminPageResponse> body = response.getBody();
            if (body != null && body.getData() != null) {
                return body.getData();
            }
        } catch (RestClientException ex) {
            log.warn("Liste entretiens admin: {}", ex.getMessage());
        }
        return new EntretienAdminPageResponse();
    }

    public List<EntretienView> listByCandidature(String candidatureId) {
        String url = entretienBaseUrl + "/entretiens/candidature/" + candidatureId;
        return fetchList(url);
    }

    public EntretienView create(CreateEntretienRequest request) {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        request.setRecruteurId(recruiter.getId().toString());

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateEntretienRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ApiResponse<EntretienView>> response = restTemplate.exchange(
                    entretienBaseUrl + "/entretiens",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<EntretienView>>() {}
            );
            ApiResponse<EntretienView> body = response.getBody();
            if (body != null && body.isSuccess() && body.getData() != null) {
                return body.getData();
            }
            String message = body != null && body.getMessage() != null
                    ? body.getMessage()
                    : "Impossible de planifier l'entretien";
            throw new EntretienServiceException(message);
        } catch (HttpStatusCodeException ex) {
            log.warn("Creation entretien HTTP {} : {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new EntretienServiceException(parseError(ex));
        } catch (RestClientException ex) {
            log.warn("Creation entretien : {}", ex.getMessage());
            throw new EntretienServiceException("Service entretien indisponible — vérifiez entretien-service (8085) et gateway (8089)");
        }
    }

    private List<EntretienView> listByRecruteur(String recruteurId) {
        String url = entretienBaseUrl + "/entretiens/recruteur/" + recruteurId;
        return fetchList(url);
    }

    private List<EntretienView> listByCandidat(String candidatId) {
        String url = entretienBaseUrl + "/entretiens/candidat/" + candidatId;
        return fetchList(url);
    }

    private List<EntretienView> fetchList(String url) {
        try {
            ResponseEntity<ApiResponse<List<EntretienView>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders()),
                    new ParameterizedTypeReference<ApiResponse<List<EntretienView>>>() {}
            );
            ApiResponse<List<EntretienView>> body = response.getBody();
            if (body != null && body.getData() != null) {
                return body.getData();
            }
            return Collections.emptyList();
        } catch (RestClientException ex) {
            log.warn("Entretiens API {} : {}", url, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpHeaders authHeaders() {
        String token = SessionAuthSupport.accessToken()
                .orElseThrow(() -> new EntretienServiceException("Session expirée — reconnectez-vous"));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private static String parseError(HttpStatusCodeException ex) {
        String raw = ex.getResponseBodyAsString();
        if (raw != null && raw.contains("\"message\"")) {
            int start = raw.indexOf("\"message\"");
            int colon = raw.indexOf(':', start);
            int firstQuote = raw.indexOf('"', colon + 1);
            int secondQuote = raw.indexOf('"', firstQuote + 1);
            if (firstQuote >= 0 && secondQuote > firstQuote) {
                return raw.substring(firstQuote + 1, secondQuote);
            }
        }
        if (ex.getStatusCode().value() == 400) {
            return "Données invalides — vérifiez la date (future), le type et le lieu/lien visio.";
        }
        if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
            return "Accès refusé — reconnectez-vous en tant que recruteur.";
        }
        return "Impossible de planifier l'entretien (" + ex.getStatusCode().value() + ").";
    }
}
