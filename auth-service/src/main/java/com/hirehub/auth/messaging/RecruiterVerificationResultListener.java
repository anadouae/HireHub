package com.hirehub.auth.messaging;

import com.hirehub.auth.service.RecruiterRegistrationService;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.events.RecruiterVerifiedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RecruiterVerificationResultListener {

    private static final Logger log = LoggerFactory.getLogger(RecruiterVerificationResultListener.class);

    private final RecruiterRegistrationService recruiterRegistrationService;

    public RecruiterVerificationResultListener(RecruiterRegistrationService recruiterRegistrationService) {
        this.recruiterRegistrationService = recruiterRegistrationService;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED)
    public void handleRecruiterVerified(RecruiterVerifiedEvent event) {
        log.info("[VERIFICATION.RESULT] Resultat recu pour userId={}, statut={}", event.getUserId(), event.getVerificationStatus());
        try {
            recruiterRegistrationService.applyVerificationResult(event);
            log.info("[VERIFICATION.RESULT] Statut applique avec succes pour userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("[VERIFICATION.RESULT] Erreur lors de l'application du resultat pour userId={}", event.getUserId(), e);
            throw e;
        }
    }
}
