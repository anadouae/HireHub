package com.hirehub.frontend.offre;

import com.hirehub.frontend.auth.HirehubUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Service
public class OffreFrontendClient {

    private static final Logger log = LoggerFactory.getLogger(OffreFrontendClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String offreBaseUrl;

    public OffreFrontendClient(@Value("${hirehub.offre-service-base-url}") String offreBaseUrl) {
        this.offreBaseUrl = offreBaseUrl;
    }

    public OffrePageResponse offresAdmin(String statut, String ville, String typeContrat, String motCle,
                                         String recruteurEmail, int page) {
        String url = UriComponentsBuilder.fromHttpUrl(offreBaseUrl + "/api/offres/admin")
                .queryParamIfPresent("statut", text(statut))
                .queryParamIfPresent("ville", text(ville))
                .queryParamIfPresent("typeContrat", text(typeContrat))
                .queryParamIfPresent("motCle", text(motCle))
                .queryParamIfPresent("recruteurEmail", text(recruteurEmail))
                .queryParam("page", page)
                .queryParam("size", 20)
                .queryParam("sort", "dateCreation,desc")
                .toUriString();
        return getPage(url, null);
    }

    @SuppressWarnings("unchecked")
    public java.util.Map<String, Long> adminOffreStats() {
        try {
            java.util.Map<String, Object> raw = restTemplate.getForObject(
                    offreBaseUrl + "/api/offres/admin/stats",
                    java.util.Map.class
            );
            if (raw == null) {
                return java.util.Map.of();
            }
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            raw.forEach((k, v) -> {
                if (v instanceof Number n) {
                    stats.put(k, n.longValue());
                }
            });
            return stats;
        } catch (RestClientException ex) {
            log.warn("Stats offres admin: {}", ex.getMessage());
            return java.util.Map.of();
        }
    }

    public OffrePageResponse offresPubliees(String ville, String typeContrat, String motCle) {
        String url = UriComponentsBuilder.fromHttpUrl(offreBaseUrl + "/api/offres")
                .queryParamIfPresent("ville", text(ville))
                .queryParamIfPresent("typeContrat", text(typeContrat))
                .queryParamIfPresent("motCle", text(motCle))
                .queryParam("page", 0)
                .queryParam("size", 20)
                .toUriString();
        return getPage(url, null);
    }

    public OffreView detail(Long id) {
        try {
            return restTemplate.getForObject(offreBaseUrl + "/api/offres/" + id, OffreView.class);
        } catch (RestClientException ex) {
            log.warn("Detail offre {} impossible: {}", id, ex.getMessage());
            return null;
        }
    }

    public OffrePageResponse mesOffres() {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        String url = offreBaseUrl + "/api/offres/mes-offres?page=0&size=50";
        return getPage(url, recruiterEntity(null, recruiter, false));
    }

    public OffreView creer(OffreForm form) {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        OffreCreateRequest body = OffreCreateRequest.fromForm(form);
        try {
            return restTemplate.postForObject(
                    offreBaseUrl + "/api/offres",
                    recruiterEntity(body, recruiter, true),
                    OffreView.class
            );
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            log.warn("Creation offre refusee ({}): {}", ex.getStatusCode(), responseBody);
            throw new OffreServiceException("Creation offre refusee: " + responseBody, ex);
        } catch (RestClientException ex) {
            log.warn("Creation offre impossible: {}", ex.getMessage());
            throw new OffreServiceException("Impossible de joindre offre-service", ex);
        }
    }

    public void publier(Long id) {
        patch("/api/offres/" + id + "/publier");
    }

    public void fermer(Long id) {
        patch("/api/offres/" + id + "/fermer");
    }

    private OffrePageResponse getPage(String url, HttpEntity<?> entity) {
        try {
            if (entity == null) {
                OffrePageResponse response = restTemplate.getForObject(url, OffrePageResponse.class);
                return response != null ? response : new OffrePageResponse();
            }
            ResponseEntity<OffrePageResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OffrePageResponse.class
            );
            return response.getBody() != null ? response.getBody() : new OffrePageResponse();
        } catch (HttpStatusCodeException ex) {
            log.warn("Liste offres refusee ({}): {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new OffreServiceException("Liste offres refusee", ex);
        } catch (RestClientException ex) {
            log.warn("Liste offres impossible: {}", ex.getMessage());
            throw new OffreServiceException("Impossible de joindre offre-service", ex);
        }
    }

    private void patch(String path) {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        HttpRequest request = HttpRequest.newBuilder(URI.create(offreBaseUrl + path))
                .header("X-User-Id", recruiter.getId().toString())
                .header("X-User-Email", recruiter.getUsername())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OffreServiceException("PATCH " + path + " -> HTTP " + response.statusCode(), null);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OffreServiceException("PATCH interrompu", ex);
        } catch (IOException ex) {
            throw new OffreServiceException("PATCH impossible", ex);
        }
    }

    private <T> HttpEntity<T> recruiterEntity(T body, HirehubUserDetails recruiter, boolean json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", recruiter.getId().toString());
        headers.add("X-User-Email", recruiter.getUsername());
        if (json) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(body, headers);
    }

    private Optional<String> text(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
