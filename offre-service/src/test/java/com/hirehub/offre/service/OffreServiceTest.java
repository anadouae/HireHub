package com.hirehub.offre.service;

import com.hirehub.offre.dto.OffreRequest;
import com.hirehub.offre.dto.OffreResponse;
import com.hirehub.offre.entity.Offre;
import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import com.hirehub.offre.repository.OffreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffreServiceTest {

    @Mock
    private OffreRepository offreRepository;

    @InjectMocks
    private OffreService offreService;

    @Test
    void creerOffreCreeUneOffreEnBrouillon() {
        when(offreRepository.save(any(Offre.class))).thenAnswer(invocation -> {
            Offre offre = invocation.getArgument(0);
            offre.setId(1L);
            return offre;
        });

        OffreResponse response = offreService.creerOffre(request(), "recruteur-10", "rh@example.com");

        assertEquals(1L, response.getId());
        assertEquals(StatutOffre.BROUILLON, response.getStatut());
        assertEquals("recruteur-10", response.getRecruteurId());
    }

    @Test
    void modifierOffreRefuseUnAutreRecruteur() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre(1L, "recruteur-10", StatutOffre.BROUILLON)));

        assertThrows(SecurityException.class, () -> offreService.modifierOffre(1L, request(), "autre-recruteur"));
        verify(offreRepository, never()).save(any());
    }

    @Test
    void publierOffreChangeLeStatut() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre(1L, "recruteur-10", StatutOffre.BROUILLON)));
        when(offreRepository.save(any(Offre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OffreResponse response = offreService.publierOffre(1L, "recruteur-10");

        assertEquals(StatutOffre.PUBLIEE, response.getStatut());
    }

    @Test
    void fermerOffreChangeLeStatut() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offre(1L, "recruteur-10", StatutOffre.PUBLIEE)));
        when(offreRepository.save(any(Offre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OffreResponse response = offreService.fermerOffre(1L, "recruteur-10");

        assertEquals(StatutOffre.FERMEE, response.getStatut());
    }

    @Test
    void getOffreInconnueRetourneErreur() {
        when(offreRepository.findById(404L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> offreService.getOffre(404L)
        );

        assertTrue(exception.getMessage().contains("Offre non trouvee"));
    }

    @Test
    void isOffreValideRetourneVraiSiPubliee() {
        when(offreRepository.existsByIdAndStatut(1L, StatutOffre.PUBLIEE)).thenReturn(true);

        assertTrue(offreService.isOffreValide(1L));
    }

    @Test
    void creerOffreEnregistreLesChampsAttendus() {
        when(offreRepository.save(any(Offre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        offreService.creerOffre(request(), "recruteur-10", "rh@example.com");

        ArgumentCaptor<Offre> captor = ArgumentCaptor.forClass(Offre.class);
        verify(offreRepository).save(captor.capture());
        assertEquals("Developpeur Java", captor.getValue().getTitre());
        assertEquals(TypeContrat.CDI, captor.getValue().getTypeContrat());
        assertEquals("Casablanca", captor.getValue().getVille());
    }

    private OffreRequest request() {
        OffreRequest request = new OffreRequest();
        request.setTitre("Developpeur Java");
        request.setDescription("Developpement backend Spring Boot");
        request.setTypeContrat(TypeContrat.CDI);
        request.setVille("Casablanca");
        request.setSalaire(12000.0);
        return request;
    }

    private Offre offre(Long id, String recruteurId, StatutOffre statut) {
        return Offre.builder()
                .id(id)
                .titre("Developpeur Java")
                .description("Developpement backend Spring Boot")
                .typeContrat(TypeContrat.CDI)
                .ville("Casablanca")
                .salaire(12000.0)
                .statut(statut)
                .recruteurId(recruteurId)
                .recruteurEmail("rh@example.com")
                .build();
    }
}
