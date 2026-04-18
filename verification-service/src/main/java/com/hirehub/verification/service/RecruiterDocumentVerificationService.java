package com.hirehub.verification.service;

import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.common.events.RecruiterRegisteredEvent;
import com.hirehub.common.events.RecruiterVerifiedEvent;
import com.hirehub.verification.client.OcrSpaceClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RecruiterDocumentVerificationService {

    private static final String STATUS_APPROVED = "APPROVED";

    private final RabbitTemplate rabbitTemplate;
    private final OcrSpaceClient ocrSpaceClient;

    public RecruiterDocumentVerificationService(RabbitTemplate rabbitTemplate, OcrSpaceClient ocrSpaceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.ocrSpaceClient = ocrSpaceClient;
    }

    @Async
    public void verifyAsync(RecruiterRegisteredEvent event) {
        VerificationResult result = runOcrOrApiVerification(event);
        String status = STATUS_APPROVED;

        RecruiterVerifiedEvent verifiedEvent = new RecruiterVerifiedEvent(
                event.getUserId(),
                result.score(),
                status,
                STATUS_APPROVED.equals(status),
                false,
                "Compte recruteur approuve automatiquement (workflow 100% automatique).",
                result.source()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.ROUTING_RECRUITER_VERIFIED,
                verifiedEvent
        );
    }

    private VerificationResult runOcrOrApiVerification(RecruiterRegisteredEvent event) {
        int score = 0;
        String source = "OCR_API";

        if (hasText(event.getRaisonSociale())) {
            score += 20;
        }
        if (hasText(event.getSiret()) && event.getSiret().trim().length() >= 8) {
            score += 25;
        }
        if (hasText(event.getPresentation()) && event.getPresentation().trim().length() >= 20) {
            score += 15;
        }

        String contentType = safeLower(event.getJustificatifContentType());
        if (contentType.contains("pdf") || contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png")) {
            score += 20;
        }

        String ocrText = "";
        if (ocrSpaceClient.isConfigured() && hasText(event.getJustificatifBase64())) {
            try {
                ocrText = safeLower(ocrSpaceClient.extractText(event.getJustificatifContentType(), event.getJustificatifBase64()));
            } catch (Exception exception) {
                source = "OCR_API_ERROR";
            }
        } else {
            source = "OCR_NOT_CONFIGURED";
        }

        if (ocrText.isBlank()) {
            // fallback minimal si OCR indisponible
            String fileName = safeLower(event.getJustificatifNom());
            ocrText = fileName;
        }

        if (ocrText.contains("registre de commerce") || ocrText.contains("rc")
                || ocrText.contains("ice") || ocrText.contains("patente")
                || ocrText.contains("identifiant fiscal") || ocrText.contains("if")) {
            score += 20;
        }

        return new VerificationResult(Math.min(score, 100), source);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record VerificationResult(int score, String source) {
    }
}
