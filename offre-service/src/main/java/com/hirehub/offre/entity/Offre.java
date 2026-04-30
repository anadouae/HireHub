package com.hirehub.offre.entity;

import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offres")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContrat;

    @Column(nullable = false)
    private String ville;

    private Double salaire;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOffre statut;

    @Column(nullable = false)
    private Long recruteurId;

    private String recruteurEmail;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutOffre.BROUILLON;
        }
    }
}