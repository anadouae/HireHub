package com.hirehub.frontend.offre;

import com.hirehub.frontend.auth.HirehubUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

    private static final String DEMO_RECRUITER_ID = "10";
    private static final String DEMO_RECRUITER_EMAIL = "rh@example.com";

    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String offreBaseUrl;

    public OffreFrontendClient(@Value("${hirehub.offre-service-base-url}") String offreBaseUrl) {
        this.offreBaseUrl = offreBaseUrl;
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
            return null;
        }
    }

    public OffrePageResponse mesOffres() {
        String url = offreBaseUrl + "/api/offres/mes-offres?page=0&size=50";
        return getPage(url, recruiterEntity(null, false));
    }

    public OffreView creer(OffreForm form) {
        return restTemplate.postForObject(
                offreBaseUrl + "/api/offres",
                recruiterEntity(form, true),
                OffreView.class
        );
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
            ResponseEntity<OffrePageResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, OffrePageResponse.class);
            return response.getBody() != null ? response.getBody() : new OffrePageResponse();
        } catch (RestClientException ex) {
            return new OffrePageResponse();
        }
    }

    private void patch(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(offreBaseUrl + path))
                .header("X-User-Id", currentUserId())
                .header("X-User-Email", currentEmail())
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RestClientException("Offre PATCH failed with status " + response.statusCode());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RestClientException("Offre PATCH interrupted", ex);
        } catch (IOException ex) {
            throw new RestClientException("Offre PATCH failed", ex);
        }
    }

    private <T> HttpEntity<T> recruiterEntity(T body, boolean json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", currentUserId());
        headers.add("X-User-Email", currentEmail());
        if (json) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(body, headers);
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof HirehubUserDetails userDetails) {
            return userDetails.getUsername();
        }
        return DEMO_RECRUITER_EMAIL;
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof HirehubUserDetails userDetails) {
            return userDetails.getId().toString();
        }
        return DEMO_RECRUITER_ID;
    }

    private Optional<String> text(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
