package com.hirehub.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecaptchaVerificationService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaVerificationService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hirehub.recaptcha.enabled:false}")
    private boolean enabled;

    @Value("${hirehub.recaptcha.secret-key:}")
    private String secretKey;

    @Value("${hirehub.recaptcha.site-key:}")
    private String siteKey;

    @Value("${hirehub.recaptcha.gcp-project-id:}")
    private String gcpProjectId;

    @Value("${hirehub.recaptcha.gcp-api-key:}")
    private String gcpApiKey;

    @Value("${hirehub.recaptcha.verify-url:https://www.google.com/recaptcha/api/siteverify}")
    private String verifyUrl;

    public boolean isValid(String token) {
        if (!enabled) {
            return true;
        }
        if (!StringUtils.hasText(token)) {
            return false;
        }

        if (enterpriseConfigured()) {
            return verifyEnterprise(token);
        }
        return verifyLegacySiteverify(token);
    }

    private boolean enterpriseConfigured() {
        return StringUtils.hasText(gcpProjectId)
                && StringUtils.hasText(gcpApiKey)
                && StringUtils.hasText(siteKey);
    }

    /**
     * reCAPTCHA Enterprise : {@code POST .../projects/{id}/assessments?key=API_KEY}
     */
    private boolean verifyEnterprise(String token) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://recaptchaenterprise.googleapis.com/v1/projects/" + gcpProjectId + "/assessments")
                .queryParam("key", gcpApiKey)
                .build(true)
                .toUriString();

        Map<String, Object> event = new HashMap<>();
        event.put("token", token);
        event.put("siteKey", siteKey);
        event.put("expectedAction", "register");
        Map<String, Object> body = new HashMap<>();
        body.put("event", event);

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                log.warn("reCAPTCHA Enterprise: reponse HTTP inattendue status={}", response.getStatusCode());
                return false;
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean valid = root.path("tokenProperties").path("valid").asBoolean(false);
            if (!valid) {
                log.warn("reCAPTCHA Enterprise: token invalide");
                return false;
            }
            String action = root.path("tokenProperties").path("action").asText("");
            if (StringUtils.hasText(action) && !"register".equals(action)) {
                return false;
            }
            double score = root.path("riskAnalysis").path("score").asDouble(0.0);
            return score >= 0.4d;
        } catch (RestClientException ex) {
            log.warn("reCAPTCHA Enterprise: appel API echoue: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.warn("reCAPTCHA Enterprise: erreur parsing", ex);
            return false;
        }
    }

    private boolean verifyLegacySiteverify(String token) {
        if (!StringUtils.hasText(secretKey)) {
            log.warn("reCAPTCHA: secret-key manquante (siteverify). Pour Enterprise, renseignez hirehub.recaptcha.gcp-project-id et gcp-api-key.");
            return false;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);

        try {
            String raw = restTemplate.postForObject(verifyUrl, body, String.class);
            if (!StringUtils.hasText(raw)) {
                return false;
            }
            JsonNode json = objectMapper.readTree(raw);
            if (!json.path("success").asBoolean(false)) {
                log.warn("reCAPTCHA siteverify: success=false error-codes={} (verifiez que la cle site et la cle secrete sont de la meme paire v3 / invisible, pas Enterprise JS + siteverify)",
                        json.path("error-codes").toString());
                return false;
            }
            double score = json.path("score").asDouble(1.0d);
            String action = json.path("action").asText("");
            if (StringUtils.hasText(action) && !"register".equals(action)) {
                return false;
            }
            return score >= 0.4d;
        } catch (RestClientException | IllegalArgumentException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
