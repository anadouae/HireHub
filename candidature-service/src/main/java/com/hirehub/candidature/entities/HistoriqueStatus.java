package com.hirehub.candidature.entities;

import com.hirehub.common.enums.CandidatureStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité HistoriqueStatus
 * Enregistre chaque changement de statut d'une candidature
 * Stockée dans PostgreSQL avec JPA/Hibernate
 */
@Entity
@Table(name = "historique_statut", indexes = {
        @Index(name = "idx_candidature_id", columnList = "candidature_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "candidature_id", nullable = false)
    private String candidatureId;

    @Column(name = "ancien_status")
    @Enumerated(EnumType.STRING)
    private CandidatureStatus ancienStatus;

    @Column(name = "nouveau_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CandidatureStatus nouveauStatus;

    @Column(name = "commentaire", length = 1000)
    private String commentaire = "";

    @Column(name = "date_changement", nullable = false, updatable = false)
    private LocalDateTime dateChangement;

    @Column(name = "utilisateur_id")
    private String utilisateurId;

    @PrePersist
    protected void onCreate() {
        dateChangement = LocalDateTime.now();
    }
}
