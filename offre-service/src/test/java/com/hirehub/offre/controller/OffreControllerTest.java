package com.hirehub.offre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hirehub.offre.dto.OffreRequest;
import com.hirehub.offre.dto.OffreResponse;
import com.hirehub.offre.enums.StatutOffre;
import com.hirehub.offre.enums.TypeContrat;
import com.hirehub.offre.service.OffreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OffreController.class)
class OffreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OffreService offreService;

    @Test
    void postCreationOffreRetourneOk() throws Exception {
        when(offreService.creerOffre(any(OffreRequest.class), eq("recruteur-10"), eq("rh@example.com")))
                .thenReturn(response(1L, StatutOffre.BROUILLON));

        mockMvc.perform(post("/api/offres")
                        .header("X-User-Id", "recruteur-10")
                        .header("X-User-Email", "rh@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("BROUILLON"));
    }

    @Test
    void getListeOffresRetournePage() throws Exception {
        Page<OffreResponse> page = new PageImpl<>(List.of(response(1L, StatutOffre.PUBLIEE)));
        when(offreService.listerOffresPubliees(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/offres")
                        .param("ville", "Casa")
                        .param("typeContrat", "CDI")
                        .param("motCle", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void patchPublierRetourneOffrePubliee() throws Exception {
        when(offreService.publierOffre(1L, "recruteur-10")).thenReturn(response(1L, StatutOffre.PUBLIEE));

        mockMvc.perform(patch("/api/offres/1/publier").header("X-User-Id", "recruteur-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PUBLIEE"));
    }

    @Test
    void idInconnuRetourne404() throws Exception {
        when(offreService.getOffre(404L)).thenThrow(new IllegalArgumentException("Offre non trouvee : 404"));

        mockMvc.perform(get("/api/offres/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void accesRefuseRetourne403() throws Exception {
        when(offreService.fermerOffre(1L, "autre-recruteur")).thenThrow(new SecurityException("Acces refuse"));

        mockMvc.perform(patch("/api/offres/1/fermer").header("X-User-Id", "autre-recruteur"))
                .andExpect(status().isForbidden());
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

    private OffreResponse response(Long id, StatutOffre statut) {
        OffreResponse response = new OffreResponse();
        response.setId(id);
        response.setTitre("Developpeur Java");
        response.setDescription("Developpement backend Spring Boot");
        response.setTypeContrat(TypeContrat.CDI);
        response.setVille("Casablanca");
        response.setSalaire(12000.0);
        response.setStatut(statut);
        response.setRecruteurId("recruteur-10");
        response.setRecruteurEmail("rh@example.com");
        return response;
    }
}
