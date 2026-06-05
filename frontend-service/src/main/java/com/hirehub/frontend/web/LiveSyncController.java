package com.hirehub.frontend.web;

import com.hirehub.common.enums.UserRole;
import com.hirehub.frontend.auth.HirehubUserDetails;
import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.entretien.EntretienFrontendClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Version légère pour rafraîchir les pages ouvertes (candidat / recruteur) sans F5 manuel.
 */
@RestController
public class LiveSyncController {

    private final EntretienFrontendClient entretienFrontendClient;
    private final CandidatureFrontendClient candidatureFrontendClient;

    public LiveSyncController(
            EntretienFrontendClient entretienFrontendClient,
            CandidatureFrontendClient candidatureFrontendClient
    ) {
        this.entretienFrontendClient = entretienFrontendClient;
        this.candidatureFrontendClient = candidatureFrontendClient;
    }

    @GetMapping("/api/live/version")
    public ResponseEntity<Map<String, Object>> version(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof HirehubUserDetails user)) {
            return ResponseEntity.ok(Map.of("version", 0));
        }
        long fingerprint = 0L;
        try {
            if (user.getRole() == UserRole.CANDIDAT) {
                int entretiens = entretienFrontendClient.listForCandidat(user).size();
                int candidatures = candidatureFrontendClient.getMyCandidatures().size();
                fingerprint = entretiens * 31L + candidatures;
                for (var c : candidatureFrontendClient.getMyCandidatures()) {
                    if (c.getStatus() != null) {
                        fingerprint += c.getStatus().hashCode();
                    }
                }
            } else if (user.getRole() == UserRole.RECRUTEUR) {
                fingerprint = entretienFrontendClient.listForRecruiter().size();
            }
        } catch (Exception ignored) {
            fingerprint = System.currentTimeMillis() / 5000L;
        }
        return ResponseEntity.ok(Map.of("version", fingerprint));
    }
}
