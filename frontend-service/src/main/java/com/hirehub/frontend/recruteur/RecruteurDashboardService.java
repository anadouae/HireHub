package com.hirehub.frontend.recruteur;

import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.candidature.CandidateDisplayService;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffreView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecruteurDashboardService {

    private static final int MAX_RECENT = 25;

    private final OffreFrontendClient offreFrontendClient;
    private final CandidatureFrontendClient candidatureFrontendClient;
    private final CandidateDisplayService candidateDisplayService;

    public RecruteurDashboardService(
            OffreFrontendClient offreFrontendClient,
            CandidatureFrontendClient candidatureFrontendClient,
            CandidateDisplayService candidateDisplayService
    ) {
        this.offreFrontendClient = offreFrontendClient;
        this.candidatureFrontendClient = candidatureFrontendClient;
        this.candidateDisplayService = candidateDisplayService;
    }

    /**
     * Candidatures « nouvelles » = statut SOUMISE, toutes offres du recruteur, les plus récentes en premier.
     */
    public List<DashboardCandidatureItem> recentNewCandidatures() {
        List<OffreView> offres = offreFrontendClient.mesOffres().getContent();
        Map<String, String> titreParOffre = offres.stream()
                .collect(Collectors.toMap(
                        o -> String.valueOf(o.getId()),
                        o -> o.getTitre() != null ? o.getTitre() : ("Offre " + o.getId()),
                        (a, b) -> a
                ));

        List<DashboardCandidatureItem> items = new ArrayList<>();
        for (OffreView offre : offres) {
            String offreId = String.valueOf(offre.getId());
            try {
                List<CandidatureDTO> candidatures = candidatureFrontendClient.getCandidaturesByOffre(offreId);
                if (candidatures == null) {
                    continue;
                }
                for (CandidatureDTO dto : candidatures) {
                    if (dto.getStatus() == null || !"SOUMISE".equalsIgnoreCase(dto.getStatus())) {
                        continue;
                    }
                    DashboardCandidatureItem item = new DashboardCandidatureItem();
                    item.setCandidatureId(dto.getId());
                    item.setOffreId(offreId);
                    item.setOffreTitre(titreParOffre.getOrDefault(offreId, "Offre"));
                    item.setCandidatDisplayName(candidateDisplayService.displayName(dto.getCandidatId()));
                    item.setCandidatEmail(candidateDisplayService.email(dto.getCandidatId()));
                    item.setStatus(dto.getStatus());
                    item.setStatusLabel("Soumise");
                    item.setDateSoumission(dto.getDateSoumission());
                    items.add(item);
                }
            } catch (Exception ignored) {
                // offre sans candidatures accessibles
            }
        }

        items.sort(Comparator.comparing(
                DashboardCandidatureItem::getDateSoumission,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        if (items.size() > MAX_RECENT) {
            return items.subList(0, MAX_RECENT);
        }
        return items;
    }

    public int countNewCandidatures() {
        return recentNewCandidatures().size();
    }
}
