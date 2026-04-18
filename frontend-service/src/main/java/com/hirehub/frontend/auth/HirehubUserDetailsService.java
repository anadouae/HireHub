package com.hirehub.frontend.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class HirehubUserDetailsService implements UserDetailsService {

    private final FrontendUserRepository frontendUserRepository;

    public HirehubUserDetailsService(FrontendUserRepository frontendUserRepository) {
        this.frontendUserRepository = frontendUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FrontendUserAccount user = frontendUserRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable."));
        return new HirehubUserDetails(user);
    }
}
