package com.hirehub.event.feign;

import com.hirehub.event.dtos.CandidatureChangedDTO;
import com.hirehub.event.dtos.CandidatureDTO;
import com.hirehub.event.dtos.EntretienPlanifiedDTO;
import com.hirehub.event.dtos.HtmlContentDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationServiceClientFallback implements NotificationServiceClientAPI {

    /**
     * @param candidature
     */
    @Override
    public void sendCandidatureConfirmation(CandidatureDTO candidature) {
        log.warn("[FALLBACK] sendCandidatureConfirmation - Candidature ID: {}, Email: {}, Offre: {}",
                candidature.getId(), candidature.getCandidatEmail(), candidature.getOffreTitle());
    }

    /**
     * @param candidatureChangedDTO
     */
    @Override
    public void sendStatutChangedNotification(CandidatureChangedDTO candidatureChangedDTO) {
        log.warn("[FALLBACK] sendStatutChangedNotification - Candidature ID: {}, Old Statut: {}, New Statut: {}",
                candidatureChangedDTO.getCandidatureId(),
                        candidatureChangedDTO.getAncienStatut(),
                        candidatureChangedDTO.getNouveauStatut());
    }

    /**
     * @param entretienPlanifiedDTO
     */
    @Override
    public void sendEntretienPlanification(EntretienPlanifiedDTO entretienPlanifiedDTO) {
        log.warn("FALLBACK send");
    }

    /**
     * @param htmlContentDTO
     */
    @Override
    public void sendHtmlEmail(HtmlContentDTO htmlContentDTO) {
        log.warn("[FALLBACK] sendHtmlEmail - Subject: {}, Recipient: {}",
                htmlContentDTO.getSubject(), htmlContentDTO.getRecipientEmail());
    }
}