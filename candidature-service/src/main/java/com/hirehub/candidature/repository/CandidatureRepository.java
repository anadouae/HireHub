package com.hirehub.candidature.repository;

import com.hirehub.candidature.entities.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour les candidatures
 * Utilise JPA/Hibernate avec PostgreSQL pour la persistance
 */
@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, String> {

    /**
     * Récupère toutes les candidatures d'un candidat
     * @param candidatId l'ID du candidat
     * @return liste des candidatures
     */
    List<Candidature> findByCandidatId(String candidatId);

    /**
     * Récupère toutes les candidatures pour une offre
     * @param offreId l'ID de l'offre
     * @return liste des candidatures
     */
    List<Candidature> findByOffreId(String offreId);

    /**
     * Vérifie si un candidat a déjà postulé à une offre
     * @param candidatId l'ID du candidat
     * @param offreId l'ID de l'offre
     * @return Optional contenant la candidature si elle existe
     */
    Optional<Candidature> findByCandidatIdAndOffreId(String candidatId, String offreId);
}
