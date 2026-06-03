package com.hirehub.frontend.oauth;

import com.hirehub.frontend.auth.FrontendUserRepository;
import com.hirehub.frontend.auth.HirehubUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOAuthAccountService {

    private final FrontendUserRepository userRepository;

    public GoogleOAuthAccountService(FrontendUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Connexion Google : uniquement si l'email existe déjà (inscription classique ou Google précédente).
     * Pas de création automatique de compte candidat.
     */
    @Transactional(readOnly = true)
    public HirehubUserDetails loadExistingFromGoogle(String email) {
        String normalized = email.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(normalized)
                .map(HirehubUserDetails::new)
                .orElseThrow(() -> new GoogleOAuthAccountNotRegisteredException(normalized));
    }
}
