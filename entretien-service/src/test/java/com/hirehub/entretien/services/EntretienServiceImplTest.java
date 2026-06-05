package com.hirehub.entretien.services;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.common.enums.InterviewStatus;
import com.hirehub.entretien.clients.CandidatureClient;
import com.hirehub.entretien.clients.CandidatureSnapshot;
import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.entities.Entretien;
import com.hirehub.entretien.entities.EntretienType;
import com.hirehub.entretien.repository.EntretienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EntretienServiceImplTest {

    private EntretienRepository repository;
    private CandidatureClient candidatureClient;
    private EntretienNotificationPublisher publisher;
    private EntretienServiceImpl service;

    @BeforeEach
    void setUp() {
        repository        = mock(EntretienRepository.class);
        candidatureClient = mock(CandidatureClient.class);
        publisher         = mock(EntretienNotificationPublisher.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-20T10:00:00Z"), ZoneId.of("UTC"));
        service = new EntretienServiceImpl(repository, candidatureClient, publisher, clock);
    }

    @Test
    void rejectsCreationWithoutRecruiterRight() {
        CreateEntretienRequest request = validRequest();
        request.setRecruteurId(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsUnknownCandidature() {
        CreateEntretienRequest request = validRequest();
        when(candidatureClient.getCandidatureById("cand-1"))
                .thenReturn(ApiResponse.error("Candidature non trouvee"));

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDuplicateEntretienForSameCandidature() {
        CreateEntretienRequest request = validRequest();
        when(candidatureClient.getCandidatureById("cand-1"))
                .thenReturn(ApiResponse.ok(candidature()));
        when(repository.existsByCandidatureIdAndStatus("cand-1", InterviewStatus.PLANIFIE))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsOverlappingRecruiterSlot() {
        CreateEntretienRequest request = validRequest();
        when(candidatureClient.getCandidatureById("cand-1"))
                .thenReturn(ApiResponse.ok(candidature()));
        when(repository.existsByRecruteurIdAndStatusAndDateHeureBetween(
                eq("rec-1"), eq(InterviewStatus.PLANIFIE), any(), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsOverlappingCandidatSlot() {
        CreateEntretienRequest request = validRequest();
        when(candidatureClient.getCandidatureById("cand-1"))
                .thenReturn(ApiResponse.ok(candidature()));
        when(repository.existsByRecruteurIdAndStatusAndDateHeureBetween(
                any(), any(), any(), any())).thenReturn(false);
        when(repository.existsByCandidatIdAndStatusAndDateHeureBetween(
                eq("user-1"), eq(InterviewStatus.PLANIFIE), any(), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void createsEntretienAndUpdatesCandidatureStatus() {
        CreateEntretienRequest request = validRequest();
        when(candidatureClient.getCandidatureById("cand-1"))
                .thenReturn(ApiResponse.ok(candidature()));
        when(repository.save(any(Entretien.class))).thenAnswer(inv -> {
            Entretien e = inv.getArgument(0);
            e.setId("ent-1");
            return e;
        });

        Entretien created = service.create(request);

        assertNotNull(created.getId());
        assertEquals(InterviewStatus.PLANIFIE, created.getStatus());
        verify(candidatureClient).updateStatus("cand-1", CandidatureStatus.ENTRETIEN.name());
        verify(publisher).publish(created, false);
    }

    @Test
    void cancelRejectsWrongRecruiter() {
        java.util.UUID entretienId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        Entretien entretien = new Entretien();
        entretien.setId(entretienId);
        entretien.setRecruteurId("rec-1");
        entretien.setStatus(InterviewStatus.PLANIFIE);
        when(repository.findById(entretienId)).thenReturn(Optional.of(entretien));

        assertThrows(SecurityException.class, () -> service.cancel(entretienId.toString(), "rec-2"));
    }

    @Test
    void cancelByCorrectRecruiterSucceeds() {
        java.util.UUID entretienId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        Entretien entretien = new Entretien();
        entretien.setId(entretienId);
        entretien.setRecruteurId("rec-1");
        entretien.setStatus(InterviewStatus.PLANIFIE);
        when(repository.findById(entretienId)).thenReturn(Optional.of(entretien));
        when(repository.save(any(Entretien.class))).thenAnswer(inv -> inv.getArgument(0));

        Entretien result = service.cancel(entretienId.toString(), "rec-1");

        assertEquals(InterviewStatus.ANNULE, result.getStatus());
        assertNotNull(result.getDateAnnulation());
        verify(publisher).publish(result, true);
    }

    private CreateEntretienRequest validRequest() {
        CreateEntretienRequest r = new CreateEntretienRequest();
        r.setCandidatureId("cand-1");
        r.setRecruteurId("rec-1");
        r.setDateHeure(LocalDateTime.of(2026, 5, 21, 10, 0));
        r.setType(EntretienType.VISIO);
        r.setLienVisio("https://meet.example/interview");
        return r;
    }

    private CandidatureSnapshot candidature() {
        CandidatureSnapshot c = new CandidatureSnapshot();
        c.setId("cand-1");
        c.setCandidatId("user-1");
        c.setOffreId("offre-1");
        c.setStatus(CandidatureStatus.EN_COURS);
        return c;
    }
}