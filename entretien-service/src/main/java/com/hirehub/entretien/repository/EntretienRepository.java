package com.hirehub.entretien.repository;

import com.hirehub.common.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.hirehub.entretien.entities.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, UUID>, JpaSpecificationExecutor<Entretien> {

    long countByStatus(InterviewStatus status);

    List<Entretien> findByCandidatureIdOrderByDateHeureDesc(String candidatureId);
    List<Entretien> findByRecruteurIdOrderByDateHeureAsc(String recruteurId);
    List<Entretien> findByCandidatIdOrderByDateHeureAsc(String candidatId);

    boolean existsByCandidatureIdAndStatus(String candidatureId, InterviewStatus status);

    boolean existsByRecruteurIdAndStatusAndDateHeureBetween(
            String recruteurId, InterviewStatus status,
            LocalDateTime start, LocalDateTime end);

    boolean existsByCandidatIdAndStatusAndDateHeureBetween(
            String candidatId, InterviewStatus status,
            LocalDateTime start, LocalDateTime end);
}