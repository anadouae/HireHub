package com.hirehub.frontend.entretien;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.frontend.auth.HirehubUserDetails;
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

    public List<EntretienView> listByCandidature(String candidatureId) {
        String url = entretienBaseUrl + "/entretiens/candidature/" + candidatureId;
        return fetchList(url);
    }

    public EntretienView create(CreateEntretienRequest request) {
        HirehubUserDetails recruiter = RecruiterContext.requireRecruiter();
        request.setRecruteurId(recruiter.getId().toString());

        HttpHeaders headers = new HttpHeaders();
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
        } catch (RestClientException ex) {
            log.warn("Creation entretien : {}", ex.getMessage());
            throw new EntretienServiceException("Service entretien indisponible");
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
                    null,
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
}
