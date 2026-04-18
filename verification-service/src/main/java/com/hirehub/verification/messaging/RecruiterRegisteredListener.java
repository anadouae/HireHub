package com.hirehub.verification.messaging;

import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.common.events.RecruiterRegisteredEvent;
import com.hirehub.verification.service.RecruiterDocumentVerificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RecruiterRegisteredListener {

    private final RecruiterDocumentVerificationService recruiterDocumentVerificationService;

    public RecruiterRegisteredListener(RecruiterDocumentVerificationService recruiterDocumentVerificationService) {
        this.recruiterDocumentVerificationService = recruiterDocumentVerificationService;
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_VERIFICATION_RECRUITER)
    public void handleRecruiterRegistered(RecruiterRegisteredEvent event) {
        recruiterDocumentVerificationService.verifyAsync(event);
    }
}
