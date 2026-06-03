package com.hirehub.frontend.candidature;

import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class CandidateDisplayService {

    private final FrontendUserRepository userRepository;

    public CandidateDisplayService(FrontendUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String displayName(String candidatId) {
        if (!StringUtils.hasText(candidatId)) {
            return "Candidat";
        }
        try {
            UUID id = UUID.fromString(candidatId.trim());
            return userRepository.findById(id)
                    .map(this::formatName)
                    .orElse(fallback(candidatId));
        } catch (IllegalArgumentException ex) {
            return fallback(candidatId);
        }
    }

    public String email(String candidatId) {
        if (!StringUtils.hasText(candidatId)) {
            return "";
        }
        try {
            return userRepository.findById(UUID.fromString(candidatId.trim()))
                    .map(FrontendUserAccount::getEmail)
                    .orElse("");
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private String formatName(FrontendUserAccount account) {
        if (StringUtils.hasText(account.getFullName())) {
            return account.getFullName().trim();
        }
        return account.getEmail();
    }

    private static String fallback(String candidatId) {
        return "Candidat (" + candidatId.substring(0, Math.min(8, candidatId.length())) + "…)";
    }
}
