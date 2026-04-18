package com.hirehub.frontend.oauth;

import com.hirehub.common.enums.UserRole;
import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import com.hirehub.frontend.auth.HirehubUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleOAuthAccountService {

    private final FrontendUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleOAuthAccountService(FrontendUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public HirehubUserDetails loadOrCreateFromGoogle(String email, String displayName) {
        String normalized = email.trim().toLowerCase();
        Optional<FrontendUserAccount> existing = userRepository.findByEmailIgnoreCase(normalized);
        if (existing.isPresent()) {
            return new HirehubUserDetails(existing.get());
        }
        String fullName = StringUtils.hasText(displayName) ? displayName.trim() : normalized;
        String randomSecret = UUID.randomUUID().toString() + UUID.randomUUID();
        FrontendUserAccount created = new FrontendUserAccount(
                UUID.randomUUID(),
                normalized,
                passwordEncoder.encode(randomSecret),
                UserRole.CANDIDAT,
                fullName,
                false,
                null
        );
        userRepository.save(created);
        return new HirehubUserDetails(created);
    }
}
