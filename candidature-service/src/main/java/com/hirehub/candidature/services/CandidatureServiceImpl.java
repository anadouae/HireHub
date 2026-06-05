package com.hirehub.candidature.services;

import com.hirehub.candidature.clients.IOffreServiceClient;
import com.hirehub.candidature.clients.OffreDTO;
import com.hirehub.candidature.config.CandidatureStateMachine;
import com.hirehub.candidature.config.InvalidTransitionException;
import com.hirehub.candidature.entities.Candidature;
import com.hirehub.candidature.entities.HistoriqueStatus;
import com.hirehub.candidature.exceptions.*;
import com.hirehub.candidature.repository.CandidatureRepository;
import com.hirehub.candidature.repository.HistoriqueStatusRepository;
import com.hirehub.candidature.security.CurrentUser;
import com.hirehub.common.constants.EventType;
import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.common.enums.UserRole;
import com.hirehub.common.notification.NotificationPublisher;
import com.hirehub.common.notification.RabbitMQConstants;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation réelle du service ICandidatureService
 * Utilise PostgreSQL via JPA et publie des événements RabbitMQ
 */
@Service
@Slf4j
public class CandidatureServiceImpl implements ICandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final HistoriqueStatusRepository historiqueStatusRepository;
    private final NotificationPublisher notificationPublisher;
    private final IOffreServiceClient iOffreServiceClient;

    public CandidatureServiceImpl(
            CandidatureRepository candidatureRepository,
            HistoriqueStatusRepository historiqueStatusRepository,
            NotificationPublisher notificationPublisher,
            IOffreServiceClient iOffreServiceClient
    ) {
        this.candidatureRepository = candidatureRepository;
        this.historiqueStatusRepository = historiqueStatusRepository;
        this.notificationPublisher = notificationPublisher;
        this.iOffreServiceClient = iOffreServiceClient;
    }

    @Override
    public Candidature createCandidatureByCandidat(Candidature candidature) {
        log.info("Création d'une candidature pour l'offre: {}", candidature.getOffreId());

        CurrentUser.requireAnyRole(UserRole.CANDIDAT);
        String candidatId = CurrentUser.requireSubject();
        candidature.setCandidatId(candidatId);
        candidature.setCandidatEmail(CurrentUser.requireEmail());

        // 1. Vérifier que l'offre existe et est publiée — on récupère le DTO complet pour avoir le titre
        OffreDTO offre;
        try {
            offre = iOffreServiceClient.getOffre(candidature.getOffreId());
            if (offre == null || !offre.isPublished()) {
                log.warn("Offre {} non trouvée ou non publiée", candidature.getOffreId());
                throw new OffreNotFoundException("L'offre n'existe pas ou n'est pas publiée.");
            }
        } catch (FeignException.NotFound e) {
            log.warn("Offre {} introuvable", candidature.getOffreId());
            throw new OffreNotFoundException("L'offre n'existe pas ou n'est pas publiée.");
        } catch (FeignException e) {
            log.error("Erreur lors de la vérification de l'offre: {}", e.getMessage(), e);
            throw new OffreNotFoundException(candidature.getOffreId(), e);
        }

        // 2. Vérifier qu'un candidat n'a jamais postulé à cette offre (y compris candidatures retirées).
        //    La requête native bypass @SQLRestriction pour inclure les enregistrements supprimés :
        //    un retrait de candidature ne donne pas le droit de re-postuler.
        if (candidatureRepository.existsByCandidatIdAndOffreIdIgnoringDeleted(
                candidature.getCandidatId(), candidature.getOffreId())) {
            log.warn("Candidat {} a déjà postulé à l'offre {} (active ou retirée)",
                    candidature.getCandidatId(), candidature.getOffreId());
            throw new CandidatureCreatedConflitException("Vous avez déjà postulé à cette offre.");
        }

        // 3. Définir les valeurs par défaut
        candidature.setStatus(CandidatureStatus.SOUMISE);
        candidature.setDateSoumission(LocalDateTime.now());

        // 4. Sauvegarder en BDD
        Candidature saved = candidatureRepository.save(candidature);
        log.info("Candidature créée avec l'ID: {}", saved.getId());

        // 5. Publier l'événement RabbitMQ (candidat + recruteur propriétaire)
        publishCandidatureCreatedEvent(saved, offre);
        return saved;
    }

    @Override
    public int rejectPendingWhenOfferClosed(String offreId) {
        log.info("Fermeture offre {} — refus des candidatures SOUMISE", offreId);
        CurrentUser.requireAnyRole(UserRole.RECRUTEUR, UserRole.ADMIN);
        String recruiterId = CurrentUser.requireSubject();

        if (!iOffreServiceClient.isRecruteurOwner(offreId, recruiterId)) {
            throw new UnauthorizedException("Vous n'êtes pas propriétaire de cette offre");
        }

        OffreDTO offre;
        try {
            offre = iOffreServiceClient.getOffre(offreId);
        } catch (FeignException e) {
            log.warn("Offre {} introuvable lors de la fermeture: {}", offreId, e.getMessage());
            throw new OffreNotFoundException("Offre introuvable");
        }

        String offerTitle = offre != null && offre.getTitre() != null ? offre.getTitre() : offreId;
        String recruiterEmail = offre != null ? offre.getRecruteurEmail() : null;
        if (recruiterEmail == null || recruiterEmail.isBlank()) {
            recruiterEmail = CurrentUser.requireEmail();
        }

        List<Candidature> pending = candidatureRepository.findByOffreId(offreId).stream()
                .filter(c -> c.getStatus() == CandidatureStatus.SOUMISE)
                .toList();

        int rejected = 0;
        for (Candidature candidature : pending) {
            CandidatureStatus oldStatus = candidature.getStatus();
            candidature.setStatus(CandidatureStatus.REFUSEE);
            candidature.setDateModification(LocalDateTime.now());
            candidatureRepository.save(candidature);

            HistoriqueStatus historique = new HistoriqueStatus();
            historique.setCandidatureId(candidature.getId());
            historique.setAncienStatus(oldStatus);
            historique.setNouveauStatus(CandidatureStatus.REFUSEE);
            historique.setCommentaire("Offre fermée — candidature non traitée");
            historique.setDateChangement(LocalDateTime.now());
            historique.setUtilisateurId(recruiterId);
            historiqueStatusRepository.save(historique);

            publishStatutChangedEvent(candidature, oldStatus, CandidatureStatus.REFUSEE,
                    "Offre fermée — candidature clôturée automatiquement");
            rejected++;
        }

        publishOfferClosedSummary(recruiterEmail, offerTitle, rejected);
        log.info("Offre {} fermée : {} candidature(s) SOUMISE → REFUSEE", offreId, rejected);
        return rejected;
    }

    @Override
    public List<Candidature> getMyCandidaturesByCandidat() {

        log.info("Récupération des candidatures du candidat");
        CurrentUser.requireAnyRole(UserRole.CANDIDAT);
        String candidatId = CurrentUser.requireSubject();
        return candidatureRepository.findByCandidatId(candidatId);
    }

    @Override
    public List<Candidature> getCandidaturesByOfferIdByRecruiter(String offerId) {

        log.info("Récupération des candidatures pour l'offre: {}", offerId);
        CurrentUser.requireAnyRole(UserRole.RECRUTEUR, UserRole.ADMIN);
        if (!CurrentUser.hasAnyRole(UserRole.ADMIN)) {
            try {
                boolean isOwner = iOffreServiceClient.isRecruteurOwner(offerId, CurrentUser.requireSubject());
                if (!isOwner) {
                    log.warn("Recruteur {} n'est pas propriétaire de l'offre {}", CurrentUser.requireSubject(), offerId);
                    throw new UnauthorizedException("Vous n'êtes pas propriétaire de cette offre");
                }
            } catch (FeignException.NotFound e) {
                log.error("Offre {} non trouvée", offerId, e);
                throw new OffreNotFoundException("Offre non trouvée");
            } catch (FeignException e) {
                log.error("Erreur lors de la vérification de propriété de l'offre: {}", e.getMessage(), e);
                throw new UnauthorizedException("Erreur de vérification d'accès à l'offre");
            }
        }
        return candidatureRepository.findByOffreId(offerId);
    }

    @Override
    public Candidature getCandidatureById(String id) {
        log.info("Récupération de la candidature: {}", id);
        Candidature candidature = candidatureRepository.findById(id).orElse(null);
        if (candidature == null) {
            return null;
        }
        requireCanReadCandidature(candidature);
        return candidature;
    }

    @Override
    public void updateCandidatureStatusByRecruiter(String candidatureId, String status) {
        log.info("Mise à jour du statut de la candidature: {} -> {}", candidatureId, status);

        CurrentUser.requireAnyRole(UserRole.RECRUTEUR, UserRole.ADMIN);

        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow( () -> new CandidatureNotFoundException("Candidature non trouvée"));

        try {
            CandidatureStatus newStatus = CandidatureStatus.valueOf(status);
            CandidatureStatus oldStatus = candidature.getStatus();

            // 1. Valider la transition de statut (machine d'états)
            if (!CandidatureStateMachine.isTransitionValid(oldStatus.name(), newStatus.name())) {
                log.warn("Transition invalide: {} -> {}", oldStatus, newStatus);
                throw new InvalidTransitionException(oldStatus.name(), newStatus.name());
            }

            // 2. Mettre à jour le statut
            candidature.setStatus(newStatus);
            candidature.setDateModification(LocalDateTime.now());
            candidatureRepository.save(candidature);

            // 3. Enregistrer dans l'historique
            HistoriqueStatus historique = new HistoriqueStatus();
            historique.setCandidatureId(candidatureId);
            historique.setAncienStatus(oldStatus);
            historique.setNouveauStatus(newStatus);
            historique.setDateChangement(LocalDateTime.now());
            historique.setUtilisateurId(CurrentUser.requireSubject());
            historiqueStatusRepository.save(historique);

            log.info("Statut de la candidature {} mis à jour de {} à {}", candidatureId, oldStatus, newStatus);

            // 4. Publier l'événement RabbitMQ
            publishStatutChangedEvent(candidature, oldStatus, newStatus, "");

        }
        catch (FeignException.NotFound e) {
            log.warn("Offre {} introuvable pour la candidature {}",
                    candidature.getOffreId(), candidatureId);
            throw new OffreNotFoundException("Offre non trouvée");
        }
        catch (IllegalArgumentException e) {
            log.error("Statut invalide: {}", status);
            throw new CandidatureChangedStatusException("Statut invalide: " + status);
        } catch (InvalidTransitionException e) {
            log.warn("Transition invalide: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateCandidatureDetailsByCandidat(String id, String CV_Path, String lettreMotivationPath) {
        log.info("Mise à jour des détails de la candidature: {}", id);

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée"));

        requireCandidatOwnerOrAdmin(candidature);

        if (CV_Path != null) {
            candidature.setCvPath(CV_Path);
        }
        if (lettreMotivationPath != null) {
            candidature.setLettreMotivationPath(lettreMotivationPath);
        }

        candidature.setDateModification(LocalDateTime.now());
        candidatureRepository.save(candidature);

        log.info("Détails de la candidature {} mis à jour", id);
    }

    @Override
    public void uploadCVAndCoverLetter(String candidatureId, String CV_Path, String lettreMotivationPath) {
        log.info("Upload des fichiers pour la candidature: {}", candidatureId);

        // Vérifier que les chemins sont fournis
        if (CV_Path == null && lettreMotivationPath == null) {
            throw new IllegalArgumentException("Au moins un fichier (CV ou lettre de motivation) doit être fourni");
        }

        // Générer les chemins relatifs dans le volume Docker
        String cvRelativePath = null;
        String lettreRelativePath = null;

        if (CV_Path != null && !CV_Path.isEmpty()) {
            // Générer un nom de fichier unique pour le CV
            String fileName = "cv_" + candidatureId + "_" + System.currentTimeMillis() + ".pdf";
            cvRelativePath = "cvs/" + fileName;
            log.info("CV sera stocké dans: {}", cvRelativePath);
        }

        if (lettreMotivationPath != null && !lettreMotivationPath.isEmpty()) {
            // Générer un nom de fichier unique pour la lettre
            String fileName = "lettre_" + candidatureId + "_" + System.currentTimeMillis() + ".pdf";
            lettreRelativePath = "lettres/" + fileName;
            log.info("Lettre de motivation sera stockée dans: {}", lettreRelativePath);
        }

        // Mettre à jour la candidature avec les nouveaux chemins
        this.updateCandidatureDetailsByCandidat(candidatureId, cvRelativePath, lettreRelativePath);

        log.info("Upload des fichiers terminé pour la candidature {}", candidatureId);
    }

    @Override
    public void deleteCandidatureByCandidat(String candidatureId) {
        log.info("Retrait de la candidature: {}", candidatureId);

        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new CandidatureNotFoundException("Candidature non trouvée"));

        requireCandidatOwnerOrAdmin(candidature);

        // Soft delete : on marque la candidature comme retirée sans la supprimer physiquement.
        // L'historique des statuts est conservé intact pour la piste d'audit RGPD.
        candidature.setDeletedAt(LocalDateTime.now());
        candidature.setDeletedBy(CurrentUser.requireSubject());
        candidatureRepository.save(candidature);

        log.info("Candidature {} retirée par {}", candidatureId, candidature.getDeletedBy());
    }

    /**
     * Publie un événement "candidature.created" dans RabbitMQ (contrat EmailEventDTO)
     */
    private void publishCandidatureCreatedEvent(Candidature candidature, OffreDTO offre) {
        try {
            String candidateEmail = candidature.getCandidatEmail();
            String offerTitle = offre != null && offre.getTitre() != null
                    ? offre.getTitre()
                    : candidature.getOffreId();

            Map<String, Object> payload = new HashMap<>();
            payload.put("candidatureId", candidature.getId());
            payload.put("offerId", candidature.getOffreId());
            payload.put("offerTitle", offerTitle);
            payload.put("status", candidature.getStatus().name());
            payload.put("cvPath", candidature.getCvPath() != null ? candidature.getCvPath() : "");
            payload.put("lettreMotivation", candidature.getLettreMotivationPath() != null ? candidature.getLettreMotivationPath() : "");

            notificationPublisher.publishEmailEvent(
                    EventType.CANDIDATURE_CREATED,
                    candidateEmail,
                    candidateEmail,
                    RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                    payload
            );

            String recruiterEmail = offre != null ? offre.getRecruteurEmail() : null;
            if (recruiterEmail != null && !recruiterEmail.isBlank()) {
                Map<String, Object> recruiterPayload = new HashMap<>(payload);
                recruiterPayload.put("candidatId", candidature.getCandidatId());
                notificationPublisher.publishEmailEvent(
                        "CANDIDATURE.RECRUITER_NEW",
                        recruiterEmail,
                        "Recruteur",
                        RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                        recruiterPayload
                );
            }

            log.info("Événement 'candidature.created' publié pour la candidature: {}", candidature.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement candidature.created: {}", e.getMessage(), e);
        }
    }

    private void publishOfferClosedSummary(String recruiterEmail, String offerTitle, int rejectedCount) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("offerTitle", offerTitle);
            payload.put("rejectedCount", rejectedCount);
            notificationPublisher.publishEmailEvent(
                    "OFFER.CLOSED",
                    recruiterEmail,
                    "Recruteur",
                    RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED,
                    payload
            );
        } catch (Exception e) {
            log.warn("Notification fermeture offre non publiée: {}", e.getMessage());
        }
    }

    /**
     * Publie un événement "candidature.statut.changed" dans RabbitMQ (contrat EmailEventDTO).
     * L'utilisateur courant est le recruteur : l'email du candidat vient de l'entité.
     */
    private void publishStatutChangedEvent(Candidature candidature, CandidatureStatus oldStatus,
                                          CandidatureStatus newStatus, String comment) {
        try {
            String candidateEmail = candidature.getCandidatEmail();
            if (candidateEmail == null || candidateEmail.isBlank()) {
                log.warn("Email du candidat absent pour la candidature {}, notification annulée", candidature.getId());
                return;
            }

            String offreTitre = candidature.getOffreId();
            try {
                OffreDTO offre = iOffreServiceClient.getOffre(candidature.getOffreId());
                if (offre != null && offre.getTitre() != null) {
                    offreTitre = offre.getTitre();
                }
            } catch (Exception ignored) {
                log.warn("Impossible de récupérer le titre de l'offre {}", candidature.getOffreId());
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("candidatureId", candidature.getId());
            payload.put("candidatId", candidature.getCandidatId());
            payload.put("offerId", candidature.getOffreId());
            payload.put("offerTitle", offreTitre);
            payload.put("oldStatus", oldStatus.name());
            payload.put("newStatus", newStatus.name());
            payload.put("comment", comment != null ? comment : "");

            notificationPublisher.publishEmailEvent(
                    EventType.CANDIDATURE_STATUT_CHANGED,
                    candidateEmail,
                    candidateEmail,
                    RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED,
                    payload
            );

            log.info("Événement 'candidature.statut.changed' publié pour la candidature: {}", candidature.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement statut.changed: {}", e.getMessage(), e);
        }
    }

    private void requireCanReadCandidature(Candidature candidature) {
        if (CurrentUser.hasAnyRole(UserRole.ADMIN)) {
            return;
        }
        UserRole role = CurrentUser.requireRole();
        if (role == UserRole.CANDIDAT) {
            if (!CurrentUser.requireSubject().equals(candidature.getCandidatId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette candidature");
            }
            return;
        }
        if (role == UserRole.RECRUTEUR) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette candidature");
    }

    private void requireCandidatOwnerOrAdmin(Candidature candidature) {
        if (CurrentUser.hasAnyRole(UserRole.ADMIN)) {
            return;
        }
        CurrentUser.requireAnyRole(UserRole.CANDIDAT);
        if (!CurrentUser.requireSubject().equals(candidature.getCandidatId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas le propriétaire de cette candidature");
        }
    }
}
