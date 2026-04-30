package com.hirehub.candidature.services;

import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.repository.CandidatureRepository;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.common.notification.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CandidatureServiceTest {
    @Mock
    private CandidatureRepository candidatureRepository;
    @Mock
    private HistoriqueStatusRepository historiqueStatusRepository;
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private CandidatureServiceImpl candidatureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        candidatureService = new CandidatureServiceImpl(
                candidatureRepository,
                historiqueStatusRepository,
                notificationPublisher
        );
    }

    /*@Test
    @DisplayName("Should retrieve all candidatures")
    void testGetMyCandidaturesByCandidat() {
        // Arrange & Act
        List<Candidature> candidatures = candidatureService.getMyCandidaturesByCandidat();

        // Assert
        assertNotNull(candidatures);
        assertTrue(candidatures.size() >= 5, "Devrait avoir au moins 5 candidatures pré-chargées");
    }*/


}