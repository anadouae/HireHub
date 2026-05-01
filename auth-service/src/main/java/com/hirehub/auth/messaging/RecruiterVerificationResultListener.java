package com.hirehub.auth.messaging;

import com.hirehub.auth.service.RecruiterRegistrationService;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.common.events.RecruiterVerifiedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RecruiterVerificationResultListener {

    private final RecruiterRegistrationService recruiterRegistrationService;

    public RecruiterVerificationResultListener(RecruiterRegistrationService recruiterRegistrationService) {
        this.recruiterRegistrationService = recruiterRegistrationService;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_AUTH_RECRUITER_VERIFIED)
    public void handleRecruiterVerified(RecruiterVerifiedEvent event) {
        recruiterRegistrationService.applyVerificationResult(event);
    }
}
