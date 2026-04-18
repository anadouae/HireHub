package com.hirehub.frontend.config;

import com.hirehub.frontend.auth.HirehubUserDetailsService;
import com.hirehub.frontend.oauth.GoogleOAuth2LoginSuccessHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleOAuthClientId;

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null || encodedPassword.isBlank()) {
                    return false;
                }
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
                // Backward compatibility for legacy plain-text rows.
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(HirehubUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /** Expose pour {@link com.hirehub.frontend.oauth.GoogleOAuth2LoginSuccessHandler} (session apres OAuth). */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            ObjectProvider<ClientRegistrationRepository> clientRegistrations,
            GoogleOAuth2LoginSuccessHandler googleOAuth2LoginSuccessHandler
    ) throws Exception {
        http.authenticationProvider(authenticationProvider);
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/css/**",
                        "/",
                        "/offres",
                        "/offres/**",
                        "/login",
                        "/register",
                        "/register/**",
                        "/demande-recruteur",
                        "/mes-candidatures",
                        "/forgot-password",
                        "/reset-password",
                        "/support",
                        "/faq",
                        "/confidentialite",
                        "/oauth2/**",
                        "/login/oauth2/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/recruteur/**").hasAnyRole("RECRUTEUR", "ADMIN")
                .requestMatchers("/candidat/**").hasAnyRole("CANDIDAT", "ADMIN")
                .anyRequest().authenticated());
        http.formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/", true));
        if (StringUtils.hasText(googleOAuthClientId) && clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(o -> o.loginPage("/login").successHandler(googleOAuth2LoginSuccessHandler));
        }
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll());
        return http.build();
    }
}
