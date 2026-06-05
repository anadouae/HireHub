package com.hirehub.entretien.services;

import com.hirehub.common.constants.EventType;
import com.hirehub.common.notification.EmailEventDTO;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.entretien.clients.CandidatureClient;
import com.hirehub.entretien.clients.CandidatureSnapshot;
import com.hirehub.entretien.clients.OffreClient;
import com.hirehub.entretien.clients.OffreTitleSnapshot;
import com.hirehub.entretien.entities.Entretien;
import com.hirehub.entretien.entities.EntretienType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EntretienNotificationPublisher {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RabbitTemplate rabbitTemplate;
    private final CandidatureClient candidatureClient;
    private final OffreClient offreClient;

    public EntretienNotificationPublisher(
            RabbitTemplate rabbitTemplate,
            CandidatureClient candidatureClient,
            OffreClient offreClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.candidatureClient = candidatureClient;
        this.offreClient = offreClient;
    }

    public void publish(Entretien entretien, boolean annule) {
        CandidatureSnapshot candidature = loadCandidature(entretien.getCandidatureId());
        String recipientEmail = resolveCandidateEmail(candidature);
        if (!StringUtils.hasText(recipientEmail)) {
            log.warn("[ENTRETIEN] Email candidat absent pour candidature {}", entretien.getCandidatureId());
            return;
        }

        String offerTitle = resolveOfferTitle(candidature);
        String candidateName = candidature != null && StringUtils.hasText(candidature.getCandidatEmail())
                ? candidature.getCandidatEmail().split("@")[0]
                : "Candidat";
        String location = entretien.getType() == EntretienType.VISIO
                ? (entretien.getLienVisio() != null ? entretien.getLienVisio() : "Visioconférence")
                : (entretien.getLieu() != null ? entretien.getLieu() : "Sur site");

        Map<String, Object> payload = new HashMap<>();
        payload.put("entretienId", entretien.getId() != null ? entretien.getId().toString() : null);
        payload.put("candidatureId", entretien.getCandidatureId());
        payload.put("offerTitle", offerTitle);
        payload.put("interviewDate", entretien.getDateHeure() != null
                ? entretien.getDateHeure().format(DATE_FMT)
                : "");
        payload.put("interviewLocation", location);
        payload.put("interviewerName", "Votre recruteur");
        if (annule) {
            payload.put("comment", "Entretien annulé par le recruteur");
        }

        String eventType = annule ? "ENTRETIEN.ANNULATION" : EventType.ENTRETIEN_PLANIFIE;

        EmailEventDTO event = new EmailEventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setRecipientEmail(recipientEmail);
        event.setRecipientName(candidateName);
        event.setPayload(payload);
        event.setCorrelationId(MDC.get("correlationId"));

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE,
                event
        );
        log.info("[ENTRETIEN] EmailEventDTO publié — type={} entretienId={}", eventType, entretien.getId());
    }

    private CandidatureSnapshot loadCandidature(String candidatureId) {
        try {
            var response = candidatureClient.getCandidatureById(candidatureId);
            if (response != null && response.isSuccess()) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("[ENTRETIEN] Impossible de charger la candidature {}: {}", candidatureId, e.getMessage());
        }
        return null;
    }

    private String resolveCandidateEmail(CandidatureSnapshot candidature) {
        if (candidature != null && StringUtils.hasText(candidature.getCandidatEmail())) {
            return candidature.getCandidatEmail();
        }
        return null;
    }

    private String resolveOfferTitle(CandidatureSnapshot candidature) {
        if (candidature == null || !StringUtils.hasText(candidature.getOffreId())) {
            return "Offre d'emploi";
        }
        try {
            Long offreId = Long.parseLong(candidature.getOffreId().trim());
            OffreTitleSnapshot offre = offreClient.getOffre(offreId);
            if (offre != null && StringUtils.hasText(offre.getTitre())) {
                return offre.getTitre();
            }
        } catch (Exception e) {
            log.warn("[ENTRETIEN] Titre offre {} indisponible: {}", candidature.getOffreId(), e.getMessage());
        }
        return "Offre #" + candidature.getOffreId();
    }
}
