package com.hirehub.frontend.recruteur;

import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffreView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecruteurStatsService {

    private final OffreFrontendClient offreFrontendClient;
    private final CandidatureFrontendClient candidatureFrontendClient;

    public RecruteurStatsService(
            OffreFrontendClient offreFrontendClient,
            CandidatureFrontendClient candidatureFrontendClient
    ) {
        this.offreFrontendClient = offreFrontendClient;
        this.candidatureFrontendClient = candidatureFrontendClient;
    }

    public RecruteurStatsView compute() {
        RecruteurStatsView stats = new RecruteurStatsView();
        List<OffreView> offres = offreFrontendClient.mesOffres().getContent();
        stats.setOffresCount(offres.size());

        long publiees = offres.stream().filter(o -> "PUBLIEE".equals(o.getStatut())).count();
        stats.setOffresPubliees(publiees);

        int totalCandidatures = 0;
        List<Integer> chartData = new ArrayList<>();
        for (OffreView offre : offres) {
            int count = 0;
            try {
                List<CandidatureDTO> candidatures = candidatureFrontendClient.getCandidaturesByOffre(
                        String.valueOf(offre.getId())
                );
                count = candidatures != null ? candidatures.size() : 0;
            } catch (Exception ignored) {
                count = 0;
            }
            totalCandidatures += count;
            chartData.add(count);
        }

        stats.setCandidaturesCount(totalCandidatures);
        stats.setCandidaturesParOffre(chartData);

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < offres.size(); i++) {
            String titre = offres.get(i).getTitre();
            if (titre != null && titre.length() > 40) {
                titre = titre.substring(0, 40) + "…";
            }
            labels.add(titre != null ? titre : "Offre " + (i + 1));
        }
        stats.setOffreLabels(labels);

        if (totalCandidatures > 0 && publiees > 0) {
            stats.setTauxReponsePercent(Math.round((double) totalCandidatures / offres.size()));
        } else {
            stats.setTauxReponsePercent(0);
        }

        return stats;
    }
}
