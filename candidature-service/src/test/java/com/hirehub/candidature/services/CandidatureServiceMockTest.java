package com.hirehub.candidature.services;

import com.hirehub.candidature.entities.Candidature;
import com.hirehub.common.enums.CandidatureStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CandidatureServiceMock
 * Ces tests s'exécutent quand le profil "mock" est actif
 */
@DisplayName("CandidatureServiceMock Tests")
@ActiveProfiles("mock")
class CandidatureServiceMockTest {

    private CandidatureServiceMock service;

    @BeforeEach
    void setUp() {
        // Initialiser le service mock
        service = new CandidatureServiceMock();
    }

    @Test
    @DisplayName("Should retrieve all candidatures")
    void testGetMyCandidaturesByCandidat() {
        // Arrange & Act
        List<Candidature> candidatures = service.getMyCandidaturesByCandidat();

        // Assert
        assertNotNull(candidatures);
        assertTrue(candidatures.size() >= 5, "Devrait avoir au moins 5 candidatures pré-chargées");
    }

    @Test
    @DisplayName("Should get candidatures by offer ID")
    void testGetCandidaturesByOfferIdByRecruiter() {
        // Arrange
        String offerId = "offre-dev-001";

        // Act
        List<Candidature> candidatures = service.getCandidaturesByOfferIdByRecruiter(offerId);

        // Assert
        assertNotNull(candidatures);
        assertEquals(4, candidatures.size(), "Devrait avoir 4 candidatures pour offre-dev-001");
        assertTrue(candidatures.stream().allMatch(c -> c.getOffreId().equals(offerId)));
    }

    @Test
    @DisplayName("Should get candidature by ID")
    void testGetCandidatureById() {
        // Arrange
        String candidatureId = "cand-001";

        // Act
        Candidature candidature = service.getCandidatureById(candidatureId);

        // Assert
        assertNotNull(candidature);
        assertEquals(candidatureId, candidature.getId());
        assertEquals("user-john-001", candidature.getCandidatId());
        assertEquals(CandidatureStatus.ACCEPTEE, candidature.getStatus());
    }

    @Test
    @DisplayName("Should return null for non-existent candidature")
    void testGetCandidatureByIdNotFound() {
        // Arrange
        String candidatureId = "cand-999";

        // Act
        Candidature candidature = service.getCandidatureById(candidatureId);

        // Assert
        assertNull(candidature);
    }

    @Test
    @DisplayName("Should create new candidature with auto-generated ID")
    void testCreateCandidatureByCandidat() {
        // Arrange
        Candidature newCandidature = new Candidature();
        newCandidature.setCandidatId("user-newcandidat");
        newCandidature.setOffreId("offre-dev-001");
        newCandidature.setCV_Path("/uploads/cv/new_cv.pdf");
        newCandidature.setLettreMotivationPath("/uploads/cover/new_cover.pdf");

        int initialCount = service.getMyCandidaturesByCandidat().size();

        // Act
        service.createCandidatureByCandidat(newCandidature);

        // Assert
        assertNotNull(newCandidature.getId(), "L'ID devrait être généré");
        assertTrue(newCandidature.getId().startsWith("cand-"));
        assertEquals(CandidatureStatus.SOUMISE, newCandidature.getStatus());

        List<Candidature> allCandidatures = service.getMyCandidaturesByCandidat();
        assertEquals(initialCount + 1, allCandidatures.size());
    }

    @Test
    @DisplayName("Should update candidature status")
    void testUpdateCandidatureStatusByRecruiter() {
        // Arrange
        String candidatureId = "cand-001";
        String newStatus = "ACCEPTEE";

        // Act
        service.updateCandidatureStatusByRecruiter(candidatureId, newStatus);

        // Assert
        Candidature updated = service.getCandidatureById(candidatureId);
        assertNotNull(updated);
        assertEquals(
                CandidatureStatus.ACCEPTEE,
                updated.getStatus()
        );
    }

    @Test
    @DisplayName("Should update candidature files")
    void testUpdateCandidatureDetailsByCandidat() {
        // Arrange
        String candidatureId = "cand-001";
        String newCVPath = "/uploads/cv/updated_cv.pdf";
        String newCoverPath = "/uploads/cover/updated_cover.pdf";

        // Act
        service.updateCandidatureDetailsByCandidat(candidatureId, newCVPath, newCoverPath);

        // Assert
        Candidature updated = service.getCandidatureById(candidatureId);
        assertNotNull(updated);
        assertEquals(newCVPath, updated.getCV_Path());
        assertEquals(newCoverPath, updated.getLettreMotivationPath());
    }

    @Test
    @DisplayName("Should delete candidature")
    void testDeleteCandidatureByCandidat() {
        // Arrange
        String candidatureId = "cand-001";
        int initialCount = service.getMyCandidaturesByCandidat().size();

        // Act
        service.deleteCandidatureByCandidat(candidatureId);

        // Assert
        Candidature deleted = service.getCandidatureById(candidatureId);
        assertNull(deleted, "Candidature devrait être supprimée");

        List<Candidature> remaining = service.getMyCandidaturesByCandidat();
        assertEquals(initialCount - 1, remaining.size());
    }

    @Test
    @DisplayName("Should handle upload CV and cover letter")
    void testUploadCVAndCoverLetter() {
        // Arrange
        String candidatureId = "cand-001";
        String newCVPath = "/uploads/cv/final_cv.pdf";
        String newCoverPath = "/uploads/cover/final_cover.pdf";

        // Act
        service.uploadCVAndCoverLetter(candidatureId, newCVPath, newCoverPath);

        // Assert
        Candidature updated = service.getCandidatureById(candidatureId);
        assertNotNull(updated);
        assertEquals(newCVPath, updated.getCV_Path());
        assertEquals(newCoverPath, updated.getLettreMotivationPath());
    }

    @Test
    @DisplayName("Should filter candidatures by offer ID correctly")
    void testGetCandidaturesByOfferIdFiltering() {
        // Arrange
        String offerId = "offre-qa-002";

        // Act
        List<Candidature> candidatures = service.getCandidaturesByOfferIdByRecruiter(offerId);

        // Assert
        assertNotNull(candidatures);
        assertEquals(1, candidatures.size(), "Devrait avoir 1 candidature pour offre-qa-002");
        assertEquals("cand-004", candidatures.get(0).getId());
    }

    @Test
    @DisplayName("Should validate status update with invalid status")
    void testUpdateCandidatureStatusWithInvalidStatus() {
        // Arrange
        String candidatureId = "cand-001";
        String invalidStatus = "INVALID_STATUS";
        CandidatureStatus originalStatus = service.getCandidatureById(candidatureId).getStatus();

        // Act
        service.updateCandidatureStatusByRecruiter(candidatureId, invalidStatus);

        // Assert - Status shouldn't change
        Candidature candidature = service.getCandidatureById(candidatureId);
        assertEquals(originalStatus, candidature.getStatus(), "Statut ne devrait pas changer avec un statut invalide");
    }

    @Test
    @DisplayName("Should handle partial update of candidature details")
    void testUpdateCandidatureDetailsWithNullValues() {
        // Arrange
        String candidatureId = "cand-001";
        Candidature original = service.getCandidatureById(candidatureId);
        String originalCV = original.getCV_Path();
        String newCoverPath = "/uploads/cover/new_cover.pdf";

        // Act
        service.updateCandidatureDetailsByCandidat(candidatureId, null, newCoverPath);

        // Assert
        Candidature updated = service.getCandidatureById(candidatureId);
        assertEquals(originalCV, updated.getCV_Path(), "CV ne devrait pas changer");
        assertEquals(newCoverPath, updated.getLettreMotivationPath(), "Cover devrait être mis à jour");
    }
}

