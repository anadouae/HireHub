package com.hirehub.entretien.services;

import com.hirehub.common.enums.InterviewStatus;
import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.dtos.EntretienAdminStats;
import com.hirehub.entretien.entities.Entretien;
import com.hirehub.entretien.entities.EntretienType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface EntretienService {
    Entretien create(CreateEntretienRequest request);
    List<Entretien> listByCandidature(String candidatureId);
    List<Entretien> listByRecruteur(String recruteurId);
    List<Entretien> listByCandidat(String candidatId);
    Entretien cancel(String entretienId, String recruteurId);
    Page<Entretien> listAdmin(InterviewStatus status, EntretienType type, String recruteurId,
                                LocalDateTime from, LocalDateTime to, Pageable pageable);
    EntretienAdminStats adminStats();
}