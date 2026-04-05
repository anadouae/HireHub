package com.hirehub.candidature.repository;

import com.hirehub.candidature.entities.HistoriqueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'historique des statuts
 */
@Repository
public interface HistoriqueStatusRepository extends JpaRepository<HistoriqueStatus, String> {

    /**
     * Récupère l'historique complet d'une candidature
     * @param candidatureId l'ID de la candidature
     * @return liste des changements de statut
     */
    List<HistoriqueStatus> findByCandidatureIdOrderByDateChangementDesc(String candidatureId);
}

