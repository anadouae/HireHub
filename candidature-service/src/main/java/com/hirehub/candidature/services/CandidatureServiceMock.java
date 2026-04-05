package com.hirehub.candidature.services;


import com.hirehub.candidature.entities.Candidature;
import com.hirehub.common.enums.CandidatureStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

// L'implémentation Mockée
@Service
@Profile("mock") // S'active SEULEMENT avec le profil 'mock'
@Slf4j
public class CandidatureServiceMock implements CandidatureService {

    // Données en mémoire (simule une BDD)
    private static final Map<String, Candidature> mockDatabase = new HashMap<>();
    private static int idCounter = 1;

    // Bloc d'initialisation statique pour peupler les données
    static {
        // Candidature 1 - En attente
        Candidature c1 = new Candidature();
        c1.setId("cand-001");
        c1.setCandidatId("user-john-001");
        c1.setOffreId("offre-dev-001");
        c1.setStatus(CandidatureStatus.EN_COURS);
        c1.setDateSoumission(LocalDateTime.now().minusDays(5));
        c1.setCV_Path("/uploads/cv/john_doe_cv.pdf");
        c1.setLettreMotivationPath("/uploads/cover/john_doe_cover.pdf");
        mockDatabase.put(c1.getId(), c1);

        // Candidature 2 - Acceptée
        Candidature c2 = new Candidature();
        c2.setId("cand-002");
        c2.setCandidatId("user-alice-002");
        c2.setOffreId("offre-dev-001");
        c2.setStatus(CandidatureStatus.ACCEPTEE);
        c2.setDateSoumission(LocalDateTime.now().minusDays(10));
        c2.setCV_Path("/uploads/cv/alice_smith_cv.pdf");
        c2.setLettreMotivationPath("/uploads/cover/alice_smith_cover.pdf");
        mockDatabase.put(c2.getId(), c2);

        // Candidature 3 - Rejetée
        Candidature c3 = new Candidature();
        c3.setId("cand-003");
        c3.setCandidatId("user-bob-003");
        c3.setOffreId("offre-dev-001");
        c3.setStatus(CandidatureStatus.REFUSEE);
        c3.setDateSoumission(LocalDateTime.now().minusDays(15));
        c3.setCV_Path("/uploads/cv/bob_johnson_cv.pdf");
        c3.setLettreMotivationPath("/uploads/cover/bob_johnson_cover.pdf");
        mockDatabase.put(c3.getId(), c3);

        // Candidature 4 - Pour une autre offre
        Candidature c4 = new Candidature();
        c4.setId("cand-004");
        c4.setCandidatId("user-carol-004");
        c4.setOffreId("offre-qa-002");
        c4.setStatus(CandidatureStatus.EN_COURS);
        c4.setDateSoumission(LocalDateTime.now().minusDays(3));
        c4.setCV_Path("/uploads/cv/carol_white_cv.pdf");
        c4.setLettreMotivationPath("/uploads/cover/carol_white_cover.pdf");
        mockDatabase.put(c4.getId(), c4);

        // Candidature 5 - En entretien
        Candidature c5 = new Candidature();
        c5.setId("cand-005");
        c5.setCandidatId("user-diana-005");
        c5.setOffreId("offre-dev-001");
        c5.setStatus(CandidatureStatus.ENTRETIEN);
        c5.setDateSoumission(LocalDateTime.now().minusDays(7));
        c5.setCV_Path("/uploads/cv/diana_brown_cv.pdf");
        c5.setLettreMotivationPath("/uploads/cover/diana_brown_cover.pdf");
        mockDatabase.put(c5.getId(), c5);

        idCounter = mockDatabase.size() + 1;
    }
    /**
     * Crée une nouvelle candidature en mémoire
     * @param candidature la candidature à créer
     */
    @Override
    public void createCandidatureByCandidat(Candidature candidature) {
        String newId = "cand-" + String.format("%03d", idCounter++);
        candidature.setId(newId);
        candidature.setDateSoumission(LocalDateTime.now());
        candidature.setStatus(CandidatureStatus.EN_COURS);
        mockDatabase.put(newId, candidature);
        log.info("[MOCK] Candidature créée: {} pour l'offre: {}", newId, candidature.getOffreId());
    }

    /**
     * Retourne les candidatures du candidat actuel
     * @return liste des candidatures du candidat
     */
    @Override
    public List<Candidature> getMyCandidaturesByCandidat() {
        // En mode mock, on retourne toutes les candidatures (pas de authentification)
        List<Candidature> result = new ArrayList<>(mockDatabase.values());
        log.info("[MOCK] Récupération de {} candidatures", result.size());
        return result;
    }

    /**
     * Retourne les candidatures pour une offre donnée (pour le recruteur)
     * @param offerId l'ID de l'offre
     * @return liste des candidatures pour cette offre
     */
    @Override
    public List<Candidature> getCandidaturesByOfferIdByRecruiter(String offerId) {
        // verifie si c'est le recruteur propriétaire de l'offre (en mode mock, on ignore cette vérification)
        List<Candidature> result = mockDatabase.values().stream()
                .filter(c -> c.getOffreId().equals(offerId))
                .toList();
        log.info("[MOCK] Récupération de {} candidatures pour l'offre: {}", result.size(), offerId);
        return result;
    }

    /**
     * Récupère une candidature par son ID
     * @param id l'ID de la candidature
     * @return la candidature ou null si non trouvée
     */
    @Override
    public Candidature getCandidatureById(String id) {
        Candidature candidature = mockDatabase.get(id);
        if (candidature != null) {
            log.info("[MOCK] Candidature trouvée: {}", id);
        } else {
            log.warn("[MOCK] Candidature non trouvée: {}", id);
        }
        return candidature;
    }

    /**
     * Met à jour le statut d'une candidature (action recruteur)
     * @param id l'ID de la candidature
     * @param status le nouveau statut
     */
    @Override
    public void updateCandidatureStatusByRecruiter(String id, String status) {
        Candidature candidature = mockDatabase.get(id);
        if (candidature != null) {
            try {
                CandidatureStatus newStatus = CandidatureStatus.valueOf(status);
                candidature.setStatus(newStatus);
                log.info("[MOCK] Statut de {} mis à jour: {}", id, status);
            } catch (IllegalArgumentException e) {
                log.error("[MOCK] Statut invalide: {}", status);
            }
        } else {
            log.warn("[MOCK] Impossible de mettre à jour: candidature {} non trouvée", id);
        }
    }

    /**
     * Met à jour les détails d'une candidature (CV et lettre de motivation)
     * @param id l'ID de la candidature
     * @param CV_Path le chemin du CV
     * @param lettreMotivationPath le chemin de la lettre de motivation
     */
    @Override
    public void updateCandidatureDetailsByCandidat(String id, String CV_Path, String lettreMotivationPath) {
        Candidature candidature = mockDatabase.get(id);
        if (candidature != null) {
            if (CV_Path != null) {
                candidature.setCV_Path(CV_Path);
            }
            if (lettreMotivationPath != null) {
                candidature.setLettreMotivationPath(lettreMotivationPath);
            }
            log.info("[MOCK] Détails de {} mis à jour", id);
        } else {
            log.warn("[MOCK] Impossible de mettre à jour: candidature {} non trouvée", id);
        }
    }

    /**
     * Upload CV et lettre de motivation
     * @param id l'ID de la candidature
     * @param CV_Path le chemin du CV
     * @param lettreMotivationPath le chemin de la lettre de motivation
     */
    @Override
    public void uploadCVAndCoverLetter(String id, String CV_Path, String lettreMotivationPath) {
        updateCandidatureDetailsByCandidat(id, CV_Path, lettreMotivationPath);
    }

    /**
     * Supprime une candidature (action candidat)
     * @param id l'ID de la candidature à supprimer
     */
    @Override
    public void deleteCandidatureByCandidat(String id) {
        Candidature removed = mockDatabase.remove(id);
        if (removed != null) {
            log.info("[MOCK] Candidature supprimée: {}", id);
        } else {
            log.warn("[MOCK] Impossible de supprimer: candidature {} non trouvée", id);
        }
    }
}