package com.hirehub.frontend.candidature;

public record CandidateProfileView(
        String fullName,
        String email,
        String phone,
        String defaultCvPath,
        boolean hasDefaultCv
) {
}
