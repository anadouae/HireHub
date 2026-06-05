package com.hirehub.candidature.repository;

import com.hirehub.candidature.entities.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hirehub.common.enums.CandidatureStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour les candidatures.
 *
 * L'entité {@link Candidature} porte {@code @SQLRestriction("deleted_at IS NULL")},
 * donc toutes les méthodes dérivées ({@code findBy*}) excluent automatiquement
 * les candidatures retirées. Aucune adaptation n'est nécessaire ici.
 *
 * La seule exception est {@link #existsByCandidatIdAndOffreIdIgnoringDeleted},
 * une requête native qui inclut les enregistrements supprimés pour empêcher
 * un candidat de re-postuler à la même offre après avoir retiré sa candidature.
 */
@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, String> {

    List<Candidature> findByCandidatId(String candidatId);

    List<Candidature> findByOffreId(String offreId);

    long countByOffreId(String offreId);

    long countByStatus(CandidatureStatus status);

    /** Exclut les supprimées — utilisé pour les vues métier normales. */
    Optional<Candidature> findByCandidatIdAndOffreId(String candidatId, String offreId);

    /**
     * Vérifie l'existence d'une candidature (active OU retirée) pour la paire
     * candidat/offre. Requête native pour bypasser {@code @SQLRestriction}.
     *
     * Utilisé à la création pour bloquer toute re-candidature, conformément
     * à la politique RGPD/ATS : un retrait ne remet pas le compteur à zéro.
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM candidatures WHERE candidat_id = :candidatId AND offre_id = :offreId",
           nativeQuery = true)
    boolean existsByCandidatIdAndOffreIdIgnoringDeleted(
            @Param("candidatId") String candidatId,
            @Param("offreId")    String offreId);
}
