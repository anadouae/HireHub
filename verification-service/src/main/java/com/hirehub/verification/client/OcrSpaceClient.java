package com.hirehub.verification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class OcrSpaceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String endpoint;
    private final String language;

    public OcrSpaceClient(
            @Value("${verification.ocr.api-key:}") String apiKey,
            @Value("${verification.ocr.endpoint:https://api.ocr.space/parse/image}") String endpoint,
            @Value("${verification.ocr.language:fre}") String language
    ) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.language = language;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public String extractText(String contentType, String base64Content) {
        String payloadBase64 = "data:" + contentType + ";base64," + base64Content;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("apikey", apiKey);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("base64Image", payloadBase64);
        form.add("language", language);
        form.add("scale", "true");
        form.add("isOverlayRequired", "false");

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(form, headers), Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            return "";
        }

        Object parsedResultsObj = body.get("ParsedResults");
        if (!(parsedResultsObj instanceof List<?> parsedResults) || parsedResults.isEmpty()) {
            return "";
        }

        Object first = parsedResults.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return "";
        }

        Object parsedText = firstMap.get("ParsedText");
        return parsedText == null ? "" : String.valueOf(parsedText);
    }
}
