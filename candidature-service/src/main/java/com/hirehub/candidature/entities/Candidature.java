package com.hirehub.candidature.entities;

import com.hirehub.common.enums.CandidatureStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Candidature
 * Stockée dans PostgreSQL avec JPA/Hibernate
 */
@Entity
@Table(name = "candidatures", indexes = {
        @Index(name = "idx_candidat_id", columnList = "candidat_id"),
        @Index(name = "idx_offre_id", columnList = "offre_id"),
        @Index(name = "idx_candidat_offre", columnList = "candidat_id, offre_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "candidat_id", nullable = false)
    private String candidatId;

    @Column(name = "offre_id", nullable = false)
    private String offreId;

    @Column(name = "cv_path")
    private String CV_Path;

    @Column(name = "lettre_motivation_path")
    private String lettreMotivationPath;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CandidatureStatus status;

    @Column(name = "date_soumission", nullable = false, updatable = false)
    private LocalDateTime dateSoumission;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateSoumission = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

}
