package com.hirehub.auth.model;

import com.hirehub.common.enums.RecruiterVerificationStatus;

public record RecruiterRegistrationStatusSnapshot(
        String userId,
        String email,
        RecruiterVerificationStatus verificationStatus,
        Integer verificationScore,
        String decisionMessage,
        String verificationSource
) {
}
