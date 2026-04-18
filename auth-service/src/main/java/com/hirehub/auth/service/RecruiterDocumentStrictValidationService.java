package com.hirehub.auth.service;

import com.hirehub.auth.dto.RegisterRecruiterForm;
import com.hirehub.auth.exception.RecruiterJustificatifMismatchException;
import com.hirehub.auth.exception.RecruiterJustificatifMismatchException.Kind;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class RecruiterDocumentStrictValidationService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private final RestTemplate restTemplate = new RestTemplate();

    private final boolean strictEnabled;
    private final String apiKey;
    private final String endpoint;
    private final String language;

    public RecruiterDocumentStrictValidationService(
            @Value("${hirehub.recruiter-verification.strict-enabled:true}") boolean strictEnabled,
            @Value("${hirehub.recruiter-verification.ocr.api-key:}") String apiKey,
            @Value("${hirehub.recruiter-verification.ocr.endpoint:https://api.ocr.space/parse/image}") String endpoint,
            @Value("${hirehub.recruiter-verification.ocr.language:fre}") String language
    ) {
        this.strictEnabled = strictEnabled;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.language = language;
    }

    /** Indique si la validation OCR / coherence formulaire-document est activee (mode strict). */
    public boolean isStrictEnabled() {
        return strictEnabled;
    }

    public void validateOrThrow(RegisterRecruiterForm form) {
        if (!strictEnabled) {
            return;
        }

        MultipartFile file = form.getJustificatifEntreprise();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le justificatif entreprise est obligatoire.");
        }

        String contentType = safeLower(file.getContentType());
        if (!(contentType.contains("pdf") || contentType.contains("jpeg")
                || contentType.contains("jpg") || contentType.contains("png"))) {
            throw new IllegalArgumentException("Format justificatif non supporte. Formats autorises : PDF, JPG, PNG.");
        }

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Validation OCR stricte indisponible : cle OCR manquante (api-key).");
        }

        String ocrText = extractText(contentType, file);
        if (!StringUtils.hasText(ocrText)) {
            throw new IllegalArgumentException("Impossible de lire le justificatif via OCR. Veuillez fournir un document plus lisible.");
        }

        String normalizedOcr = normalize(ocrText);
        boolean companyMatch = matchesCompanyName(form.getRaisonSociale(), normalizedOcr);
        boolean siretMatch = matchesSiret(form.getSiret(), normalizedOcr);

        if (!companyMatch || !siretMatch) {
            Kind kind = !companyMatch && !siretMatch
                    ? Kind.BOTH
                    : (!companyMatch ? Kind.RAISON_SOCIALE : Kind.SIRET);
            throw new RecruiterJustificatifMismatchException(kind);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String extractText(String contentType, MultipartFile file) {
        String base64 = toBase64(file);
        String payloadBase64 = "data:" + contentType + ";base64," + base64;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("apikey", apiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("base64Image", payloadBase64);
        body.add("language", language);
        body.add("scale", "true");
        body.add("isOverlayRequired", "false");

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(body, headers), Map.class);
        Map responseBody = response.getBody();
        if (responseBody == null) {
            return "";
        }

        Object parsedResultsObj = responseBody.get("ParsedResults");
        if (!(parsedResultsObj instanceof List)) {
            return "";
        }
        List parsedResults = (List) parsedResultsObj;
        if (parsedResults.isEmpty()) {
            return "";
        }

        Object first = parsedResults.get(0);
        if (!(first instanceof Map)) {
            return "";
        }
        Map firstMap = (Map) first;
        Object parsedText = firstMap.get("ParsedText");
        return parsedText == null ? "" : String.valueOf(parsedText);
    }

    private boolean matchesCompanyName(String expectedCompanyName, String normalizedOcr) {
        String normalizedCompany = normalize(expectedCompanyName);
        if (!StringUtils.hasText(normalizedCompany)) {
            return false;
        }
        if (normalizedOcr.contains(normalizedCompany)) {
            return true;
        }

        String[] tokens = normalizedCompany.split("\\s+");
        int significantTokens = 0;
        int matchedTokens = 0;
        for (String token : tokens) {
            if (token.length() < 3) {
                continue;
            }
            significantTokens++;
            if (normalizedOcr.contains(token)) {
                matchedTokens++;
            }
        }
        if (significantTokens == 0) {
            return normalizedOcr.contains(normalizedCompany);
        }

        double ratio = (double) matchedTokens / (double) significantTokens;
        return ratio >= 0.60d;
    }

    private boolean matchesSiret(String expectedSiret, String normalizedOcr) {
        if (!StringUtils.hasText(expectedSiret)) {
            return true;
        }
        String digits = expectedSiret.replaceAll("\\D+", "");
        if (!StringUtils.hasText(digits)) {
            return true;
        }
        return normalizedOcr.replace(" ", "").contains(digits);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String ascii = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        String lower = ascii.toLowerCase(Locale.ROOT);
        return NON_ALNUM.matcher(lower).replaceAll(" ").trim().replaceAll("\\s+", " ");
    }

    private String toBase64(MultipartFile file) {
        try {
            return java.util.Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Impossible de lire le justificatif entreprise.", exception);
        }
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
