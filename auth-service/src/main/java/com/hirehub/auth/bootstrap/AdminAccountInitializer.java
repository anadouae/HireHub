package com.hirehub.auth.bootstrap;

import com.hirehub.auth.model.UserAccount;
import com.hirehub.auth.repo.UserAccountRepository;
import com.hirehub.common.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hirehub.admin.email}")
    private String adminEmail;

    @Value("${hirehub.admin.password}")
    private String adminPassword;

    @Value("${hirehub.admin.full-name}")
    private String adminFullName;

    public AdminAccountInitializer(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase();
        userAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> userAccountRepository.save(
                        new UserAccount(
                                UUID.randomUUID(),
                                normalizedEmail,
                                passwordEncoder.encode(adminPassword),
                                UserRole.ADMIN,
                                adminFullName == null || adminFullName.isBlank() ? "Admin HireHub" : adminFullName.trim(),
                                true,
                                null
                        )
                ));
    }
}
