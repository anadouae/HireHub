package com.hirehub.frontend.entretien;

import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffreView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EntretienDisplayEnrichmentService {

    private final CandidatureFrontendClient candidatureFrontendClient;
    private final OffreFrontendClient offreFrontendClient;
    private final FrontendUserRepository frontendUserRepository;

    public EntretienDisplayEnrichmentService(
            CandidatureFrontendClient candidatureFrontendClient,
            OffreFrontendClient offreFrontendClient,
            FrontendUserRepository frontendUserRepository
    ) {
        this.candidatureFrontendClient = candidatureFrontendClient;
        this.offreFrontendClient = offreFrontendClient;
        this.frontendUserRepository = frontendUserRepository;
    }

    public void enrich(List<EntretienView> entretiens) {
        if (entretiens == null) {
            return;
        }
        for (EntretienView entretien : entretiens) {
            enrichOne(entretien);
        }
    }

    public void enrichOne(EntretienView entretien) {
        if (entretien == null || entretien.getCandidatureId() == null) {
            return;
        }
        Optional<CandidatureDTO> candidature = candidatureFrontendClient.getCandidature(entretien.getCandidatureId());
        candidature.ifPresent(c -> {
            entretien.setOffreId(c.getOffreId());
            if (c.getOffreId() != null) {
                try {
                    OffreView offre = offreFrontendClient.detail(Long.parseLong(c.getOffreId()));
                    if (offre != null && offre.getTitre() != null) {
                        entretien.setOffreTitre(offre.getTitre());
                    }
                } catch (NumberFormatException ignored) {
                    entretien.setOffreTitre("Offre #" + c.getOffreId());
                }
            }
        });
        if (entretien.getOffreTitre() == null) {
            entretien.setOffreTitre("Offre");
        }
        entretien.setRecruteurLabel(resolveRecruteurLabel(entretien.getRecruteurId()));
    }

    public String resolveRecruteurLabel(String recruteurId) {
        if (recruteurId == null || recruteurId.isBlank()) {
            return "—";
        }
        try {
            UUID id = UUID.fromString(recruteurId.trim());
            Optional<FrontendUserAccount> account = frontendUserRepository.findById(id);
            if (account.isPresent()) {
                FrontendUserAccount u = account.get();
                if (u.getFullName() != null && !u.getFullName().isBlank()) {
                    return u.getFullName();
                }
                return u.getEmail();
            }
        } catch (IllegalArgumentException ignored) {
            // not a UUID
        }
        return recruteurId;
    }
}
