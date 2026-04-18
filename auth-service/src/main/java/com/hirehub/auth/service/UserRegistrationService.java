package com.hirehub.auth.service;

import com.hirehub.auth.dto.RegisterCandidateForm;
import com.hirehub.auth.dto.RegisterRecruiterForm;
import com.hirehub.auth.model.UserAccount;
import com.hirehub.auth.repo.UserAccountRepository;
import com.hirehub.common.enums.RecruiterVerificationStatus;
import com.hirehub.common.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserRegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount registerCandidate(RegisterCandidateForm form) {
        if (userAccountRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalStateException("Un compte existe deja avec cet email.");
        }
        UUID id = UUID.randomUUID();
        UserAccount user = new UserAccount(
                id,
                form.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(form.getPassword()),
                UserRole.CANDIDAT,
                form.getNomComplet().trim(),
                false,
                null
        );
        return userAccountRepository.save(user);
    }

    @Transactional
    public UserAccount registerRecruiter(RegisterRecruiterForm form) {
        if (userAccountRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalStateException("Un compte existe deja avec cet email.");
        }
        UUID id = UUID.randomUUID();
        UserAccount user = new UserAccount(
                id,
                form.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(form.getPassword()),
                UserRole.RECRUTEUR,
                form.getNomComplet().trim(),
                true,
                RecruiterVerificationStatus.APPROVED
        );
        user.setCompanyName(form.getRaisonSociale().trim());
        if (form.getSiret() != null && !form.getSiret().isBlank()) {
            user.setCompanySiret(form.getSiret().trim());
        }
        user.setCompanyPresentation(form.getPresentation().trim());
        return userAccountRepository.save(user);
    }

    @Transactional
    public void applyRecruiterVerification(UUID userId, RecruiterVerificationStatus status) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable: " + userId));
        user.setVerificationStatus(status);
        if (status == RecruiterVerificationStatus.APPROVED) {
            user.setRecruiterApproved(true);
        } else if (status == RecruiterVerificationStatus.REVIEW_REQUIRED) {
            user.setRecruiterApproved(false);
        }
        userAccountRepository.save(user);
    }
}
