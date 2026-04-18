package com.hirehub.frontend.password;

import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final FrontendUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(
            FrontendUserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSender
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    /**
     * Demande de reset : reponse identique que l'email existe ou non (evite l'enumeration).
     * Envoie un code a 6 chiffres (valide 15 min).
     */
    @Transactional
    public void requestReset(String emailRaw) {
        if (!StringUtils.hasText(emailRaw)) {
            return;
        }
        String email = emailRaw.trim().toLowerCase();
        Optional<FrontendUserAccount> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            log.info("Demande reset MDP : email inconnu (reponse neutre au client).");
            return;
        }
        FrontendUserAccount user = userOpt.get();
        tokenRepository.deleteByUserId(user.getId());

        String digits = randomOtp6();
        String hash = otpHashForUser(user.getId(), digits);
        PasswordResetToken entity = new PasswordResetToken(
                UUID.randomUUID(),
                user.getId(),
                hash,
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );
        tokenRepository.save(entity);

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(user.getEmail());
                msg.setSubject("HireHub - reinitialisation du mot de passe");
                String body = "Bonjour,\n\n"
                        + "Votre code de verification (valide 15 minutes) :\n\n"
                        + digits + "\n\n"
                        + "Si vous n'etes pas a l'origine de cette demande, ignorez ce message.\n";
                msg.setText(body);
                sender.send(msg);
                log.info("Code reset MDP envoye a {}", user.getEmail());
            } catch (Exception ex) {
                log.warn("Envoi email reset echoue pour {} : {}", user.getEmail(), ex.getMessage());
                log.warn("CODE RESET MDP (copier manuellement) pour {} : {}", user.getEmail(), digits);
            }
        } else {
            log.warn("spring.mail non configure - CODE RESET MDP pour {} code={}", user.getEmail(), digits);
        }
    }

    @Transactional
    public void completeResetWithEmailAndCode(String emailRaw, String codeRaw, String newPassword) {
        if (!StringUtils.hasText(emailRaw) || !StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new IllegalArgumentException("Donnees invalides.");
        }
        Optional<String> digitsOpt = normalizeOtpDigits(codeRaw);
        if (digitsOpt.isEmpty()) {
            throw new IllegalArgumentException("Code invalide.");
        }
        String email = emailRaw.trim().toLowerCase();
        FrontendUserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Code ou email invalide."));
        PasswordResetToken token = tokenRepository.findActiveForUser(user.getId(), Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Code ou email invalide."));
        String expectedHash = otpHashForUser(user.getId(), digitsOpt.get());
        if (!expectedHash.equals(token.getTokenHash())) {
            throw new IllegalArgumentException("Code ou email invalide.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        token.setConsumed(true);
        tokenRepository.save(token);
        log.info("Mot de passe reinitialise pour {}", user.getEmail());
    }

    private String randomOtp6() {
        int n = 100000 + random.nextInt(900000);
        return String.valueOf(n);
    }

    /** Meme regle que {@code SignupEmailVerificationService.normalizeOtp} (6 chiffres, espaces toleres). */
    private static Optional<String> normalizeOtpDigits(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        String d = raw.replaceAll("\\D", "");
        if (d.length() < 6) {
            return Optional.empty();
        }
        d = d.length() > 6 ? d.substring(d.length() - 6) : d;
        return d.length() == 6 ? Optional.of(d) : Optional.empty();
    }

    private static String otpHashForUser(UUID userId, String sixDigits) {
        return sha256Hex(userId + ":" + sixDigits);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
