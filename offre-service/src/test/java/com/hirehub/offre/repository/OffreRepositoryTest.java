package com.hirehub.offre.repository;

import com.hirehub.offre.entity.Offre;
import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OffreRepositoryTest {

    @Autowired
    private OffreRepository offreRepository;

    @Test
    void findWithFiltersRetourneSeulementLesOffresPubliees() {
        offreRepository.save(offre("Developpeur Java", "Casablanca", TypeContrat.CDI, StatutOffre.PUBLIEE));
        offreRepository.save(offre("Designer", "Rabat", TypeContrat.CDD, StatutOffre.BROUILLON));

        Page<Offre> result = offreRepository.findWithFilters(
                StatutOffre.PUBLIEE,
                "casa",
                TypeContrat.CDI,
                "java",
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Developpeur Java", result.getContent().get(0).getTitre());
    }

    @Test
    void existsByIdAndStatutValideUneOffrePubliee() {
        Offre saved = offreRepository.save(offre("Developpeur Java", "Casablanca", TypeContrat.CDI, StatutOffre.PUBLIEE));

        assertTrue(offreRepository.existsByIdAndStatut(saved.getId(), StatutOffre.PUBLIEE));
        assertFalse(offreRepository.existsByIdAndStatut(saved.getId(), StatutOffre.FERMEE));
    }

    private Offre offre(String titre, String ville, TypeContrat typeContrat, StatutOffre statut) {
        return Offre.builder()
                .titre(titre)
                .description("Description de test")
                .typeContrat(typeContrat)
                .ville(ville)
                .salaire(10000.0)
                .statut(statut)
                .recruteurId("recruteur-10")
                .recruteurEmail("rh@example.com")
                .build();
    }
}
