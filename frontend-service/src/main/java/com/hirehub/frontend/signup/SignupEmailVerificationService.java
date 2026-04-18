package com.hirehub.frontend.signup;

import com.hirehub.frontend.auth.FrontendUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class SignupEmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(SignupEmailVerificationService.class);

    private static final String ROLE_CANDIDAT = "CANDIDAT";
    private static final String ROLE_RECRUTEUR = "RECRUTEUR";

    private final SignupEmailChallengeRepository challengeRepository;
    private final FrontendUserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final SecureRandom random = new SecureRandom();

    public SignupEmailVerificationService(
            SignupEmailChallengeRepository challengeRepository,
            FrontendUserRepository userRepository,
            ObjectProvider<JavaMailSender> mailSender
    ) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    /**
     * Envoie un code 6 chiffres. Echec si email deja inscrit (frontend) ou role invalide.
     *
     * @return message cle pour affichage flash : ok, email_taken, invalid
     */
    @Transactional
    public String requestCode(String emailRaw, String nomRaw, String roleRaw) {
        if (!StringUtils.hasText(emailRaw) || !StringUtils.hasText(nomRaw)) {
            return "invalid";
        }
        String role = normalizeRole(roleRaw);
        if (role == null) {
            return "invalid";
        }
        String email = emailRaw.trim().toLowerCase();
        String nom = nomRaw.trim();
        if (nom.length() > 500) {
            return "invalid";
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return "email_taken";
        }

        challengeRepository.deletePendingByEmailAndRole(email, role);

        String digits = randomOtp6();
        String hash = sha256Hex(email + ":" + digits);
        SignupEmailChallenge entity = new SignupEmailChallenge(
                UUID.randomUUID(),
                email,
                nom,
                role,
                hash,
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );
        challengeRepository.save(entity);

        JavaMailSender sender = mailSender.getIfAvailable();
        String subject = role.equals(ROLE_CANDIDAT)
                ? "HireHub - code verification inscription candidat"
                : "HireHub - code verification inscription recruteur";
        String body = "Bonjour " + nom + ",\n\n"
                + "Votre code de verification HireHub (valide 15 min) :\n\n"
                + digits + "\n\n"
                + "Si vous n'etes pas a l'origine de cette demande, ignorez ce message.\n";

        if (sender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(email);
                msg.setSubject(subject);
                msg.setText(body);
                sender.send(msg);
                log.info("Code inscription envoye a {}", email);
            } catch (Exception ex) {
                log.warn("Envoi code inscription echoue pour {} : {}", email, ex.getMessage());
                log.warn("CODE INSCRIPTION (copier manuellement) pour {} : {}", email, digits);
            }
        } else {
            log.warn("spring.mail non configure - CODE INSCRIPTION pour {} : {}", email, digits);
        }
        return "ok";
    }

    @Transactional(readOnly = true)
    public Optional<SignupEmailChallenge> findPendingChallenge(String emailRaw, String roleRaw) {
        String role = normalizeRole(roleRaw);
        if (role == null || !StringUtils.hasText(emailRaw)) {
            return Optional.empty();
        }
        String email = emailRaw.trim().toLowerCase();
        return challengeRepository.findTopByEmailAndRoleAndConsumedFalseOrderByExpiresAtDesc(email, role)
                .filter(c -> c.getExpiresAt().isAfter(Instant.now()));
    }

    /**
     * @return empty si code invalide ou expire
     */
    @Transactional
    public Optional<SignupSessionVerified> verifyAndConsume(String emailRaw, String roleRaw, String codeRaw) {
        String role = normalizeRole(roleRaw);
        if (role == null || !StringUtils.hasText(emailRaw)) {
            return Optional.empty();
        }
        String email = emailRaw.trim().toLowerCase();
        Optional<String> digitsOpt = normalizeOtp(codeRaw);
        if (digitsOpt.isEmpty()) {
            return Optional.empty();
        }
        String hash = sha256Hex(email + ":" + digitsOpt.get());
        Optional<SignupEmailChallenge> opt = challengeRepository
                .findTopByEmailAndRoleAndConsumedFalseOrderByExpiresAtDesc(email, role);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        SignupEmailChallenge c = opt.get();
        if (c.isConsumed() || c.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        if (!c.getCodeHash().equals(hash)) {
            return Optional.empty();
        }
        c.setConsumed(true);
        challengeRepository.save(c);
        return Optional.of(new SignupSessionVerified(c.getEmail(), c.getNomComplet(), c.getRole()));
    }

    private static String normalizeRole(String roleRaw) {
        if (!StringUtils.hasText(roleRaw)) {
            return null;
        }
        String r = roleRaw.trim().toUpperCase();
        if (ROLE_CANDIDAT.equals(r) || ROLE_RECRUTEUR.equals(r)) {
            return r;
        }
        return null;
    }

    private String randomOtp6() {
        int n = 100000 + random.nextInt(900000);
        return String.valueOf(n);
    }

    public static Optional<String> normalizeOtp(String raw) {
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

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
