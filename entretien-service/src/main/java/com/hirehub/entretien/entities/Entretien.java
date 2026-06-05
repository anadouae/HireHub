package com.hirehub.entretien.entities;

import com.hirehub.common.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "entretiens", indexes = {
        @Index(name = "idx_entretien_candidature",    columnList = "candidature_id"),
        @Index(name = "idx_entretien_recruteur_date", columnList = "recruteur_id, date_heure"),
        @Index(name = "idx_entretien_candidat_date",  columnList = "candidat_id, date_heure")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Entretien {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "candidature_id", nullable = false)
    private String candidatureId;

    @Column(name = "candidat_id", nullable = false)
    private String candidatId;

    @Column(name = "recruteur_id", nullable = false)
    private String recruteurId;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "lieu")
    private String lieu;

    @Column(name = "lien_visio")
    private String lienVisio;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EntretienType type;

    @Column(name = "notes_internes", length = 2000)
    private String notesInternes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    @PrePersist
    void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = dateCreation;
    }

    @PreUpdate
    void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}