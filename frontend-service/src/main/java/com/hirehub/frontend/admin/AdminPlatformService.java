package com.hirehub.frontend.admin;

import com.hirehub.frontend.candidature.CandidatureFrontendClient;
import com.hirehub.frontend.entretien.EntretienAdminPageResponse;
import com.hirehub.frontend.entretien.EntretienDisplayEnrichmentService;
import com.hirehub.frontend.entretien.EntretienFrontendClient;
import com.hirehub.frontend.entretien.EntretienView;
import com.hirehub.frontend.offre.OffreFrontendClient;
import com.hirehub.frontend.offre.OffrePageResponse;
import com.hirehub.frontend.offre.OffreView;
import com.hirehub.frontend.clients.CandidatureDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminPlatformService {

    private static final Logger log = LoggerFactory.getLogger(AdminPlatformService.class);

    private final AdminSpaceService adminSpaceService;
    private final OffreFrontendClient offreFrontendClient;
    private final EntretienFrontendClient entretienFrontendClient;
    private final CandidatureFrontendClient candidatureFrontendClient;
    private final EntretienDisplayEnrichmentService entretienDisplayEnrichmentService;

    public AdminPlatformService(
            AdminSpaceService adminSpaceService,
            OffreFrontendClient offreFrontendClient,
            EntretienFrontendClient entretienFrontendClient,
            CandidatureFrontendClient candidatureFrontendClient,
            EntretienDisplayEnrichmentService entretienDisplayEnrichmentService
    ) {
        this.adminSpaceService = adminSpaceService;
        this.offreFrontendClient = offreFrontendClient;
        this.entretienFrontendClient = entretienFrontendClient;
        this.candidatureFrontendClient = candidatureFrontendClient;
        this.entretienDisplayEnrichmentService = entretienDisplayEnrichmentService;
    }

    public AdminDashboardStats userStats() {
        return adminSpaceService.dashboardStats();
    }

    public AdminPlatformStats platformStats() {
        try {
            Map<String, Long> offreStats = offreFrontendClient.adminOffreStats();
            Map<String, Long> candStats = candidatureFrontendClient.adminStats();
            Map<String, Long> entStats = entretienFrontendClient.adminStats();

            long brouillon = mapLong(offreStats, "brouillon");
            long publiee = mapLong(offreStats, "publiee");
            long fermee = mapLong(offreStats, "fermee");
            long candTotal = mapLong(candStats, "total");
            long soumises = mapLong(candStats, "SOUMISE");
            long entTotal = mapLong(entStats, "total");
            long planifies = mapLong(entStats, "planifies");
            long annules = mapLong(entStats, "annules");

            if (entTotal == 0) {
                EntretienAdminPageResponse page = entretienFrontendClient.listAdmin(null, null, null, 0);
                entTotal = page.getTotalElements() > 0 ? page.getTotalElements() : page.getContent().size();
                planifies = page.getContent().stream().filter(e -> "PLANIFIE".equals(e.getStatus())).count();
                annules = page.getContent().stream().filter(e -> "ANNULE".equals(e.getStatus())).count();
            }
            if (publiee == 0 && brouillon == 0 && fermee == 0) {
                OffrePageResponse offres = offreFrontendClient.offresAdmin(null, null, null, null, null, 0);
                if (offres.getContent() != null) {
                    for (OffreView o : offres.getContent()) {
                        if ("PUBLIEE".equals(o.getStatut())) publiee++;
                        else if ("FERMEE".equals(o.getStatut())) fermee++;
                        else if ("BROUILLON".equals(o.getStatut())) brouillon++;
                    }
                }
                if (offres.getTotalElements() > 0 && publiee + fermee + brouillon == 0) {
                    publiee = offres.getTotalElements();
                }
            }

            return new AdminPlatformStats(brouillon, publiee, fermee, candTotal, soumises, entTotal, planifies, annules);
        } catch (Exception ex) {
            log.warn("KPI admin plateforme: {}", ex.getMessage());
            return AdminPlatformStats.empty();
        }
    }

    public List<AdminOffreRow> listOffres(String statut, String ville, String typeContrat, String motCle,
                                         String recruteurEmail, int page) {
        OffrePageResponse pageResponse = offreFrontendClient.offresAdmin(statut, ville, typeContrat, motCle, recruteurEmail, page);
        List<AdminOffreRow> rows = new ArrayList<>();
        for (OffreView offre : pageResponse.getContent()) {
            long count = candidatureFrontendClient.countByOffre(String.valueOf(offre.getId()));
            rows.add(new AdminOffreRow(offre, count));
        }
        return rows;
    }

    public OffrePageResponse offresPageMeta(String statut, String ville, String typeContrat, String motCle,
                                            String recruteurEmail, int page) {
        return offreFrontendClient.offresAdmin(statut, ville, typeContrat, motCle, recruteurEmail, page);
    }

    public OffreView offreDetail(Long id) {
        return offreFrontendClient.detail(id);
    }

    public List<CandidatureDTO> candidaturesForOffre(String offreId) {
        return candidatureFrontendClient.getCandidaturesByOffreAdmin(offreId);
    }

    public EntretienAdminPageResponse listEntretiens(String status, String type, String recruteurId, int page) {
        EntretienAdminPageResponse response = entretienFrontendClient.listAdmin(status, type, recruteurId, page);
        entretienDisplayEnrichmentService.enrich(response.getContent());
        if (response.getTotalElements() == 0 && !response.getContent().isEmpty()) {
            response.setTotalElements(response.getContent().size());
        }
        return response;
    }

    public List<AdminEntretienRow> listEntretienRows(String status, String type, String recruteurId, int pageIndex) {
        EntretienAdminPageResponse page = listEntretiens(status, type, recruteurId, pageIndex);
        List<AdminEntretienRow> rows = new ArrayList<>();
        for (EntretienView e : page.getContent()) {
            rows.add(new AdminEntretienRow(e));
        }
        return rows;
    }

    private static long mapLong(Map<String, Long> map, String key) {
        if (map == null) {
            return 0L;
        }
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}
