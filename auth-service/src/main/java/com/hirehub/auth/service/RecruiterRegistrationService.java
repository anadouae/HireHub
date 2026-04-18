package com.hirehub.auth.service;

import com.hirehub.auth.dto.RegisterRecruiterForm;
import com.hirehub.auth.model.RecruiterRegistrationStatusSnapshot;
import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.common.enums.RecruiterVerificationStatus;
import com.hirehub.common.events.RecruiterRegisteredEvent;
import com.hirehub.common.events.RecruiterVerifiedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecruiterRegistrationService {

    private final RabbitTemplate rabbitTemplate;
    private final UserRegistrationService userRegistrationService;
    private final Map<String, RecruiterRegistrationStatusSnapshot> statuses = new ConcurrentHashMap<>();

    public RecruiterRegistrationService(RabbitTemplate rabbitTemplate, UserRegistrationService userRegistrationService) {
        this.rabbitTemplate = rabbitTemplate;
        this.userRegistrationService = userRegistrationService;
    }

    public void savePendingRegistration(String userId, RegisterRecruiterForm form, String justificatifNom) {
        statuses.put(
                userId,
                new RecruiterRegistrationStatusSnapshot(
                        userId,
                        form.getEmail(),
                        RecruiterVerificationStatus.PENDING_AUTO_CHECK,
                        null,
                        "Verification OCR/API en cours pour le document: " + justificatifNom,
                        "VERIFICATION_SERVICE_PENDING"
                )
        );
    }

    public void publishRegisteredEvent(RecruiterRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.ROUTING_RECRUITER_REGISTERED,
                event
        );
    }

    public void applyVerificationResult(RecruiterVerifiedEvent event) {
        RecruiterVerificationStatus status = RecruiterVerificationStatus.valueOf(event.getVerificationStatus());
        userRegistrationService.applyRecruiterVerification(UUID.fromString(event.getUserId()), status);

        RecruiterRegistrationStatusSnapshot previous = statuses.get(event.getUserId());
        String email = previous != null ? previous.email() : null;

        statuses.put(
                event.getUserId(),
                new RecruiterRegistrationStatusSnapshot(
                        event.getUserId(),
                        email,
                        status,
                        event.getVerificationScore(),
                        event.getDecisionMessage(),
                        event.getVerificationSource()
                )
        );
    }

    public Optional<RecruiterRegistrationStatusSnapshot> getStatus(String userId) {
        return Optional.ofNullable(statuses.get(userId));
    }
}
