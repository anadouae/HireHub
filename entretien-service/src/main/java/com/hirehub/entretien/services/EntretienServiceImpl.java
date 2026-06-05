package com.hirehub.entretien.services;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.common.enums.InterviewStatus;
import com.hirehub.entretien.clients.CandidatureClient;
import com.hirehub.entretien.clients.CandidatureSnapshot;
import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.dtos.EntretienAdminStats;
import com.hirehub.entretien.entities.Entretien;
import com.hirehub.entretien.entities.EntretienType;
import com.hirehub.entretien.repository.EntretienRepository;
import feign.FeignException;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EntretienServiceImpl implements EntretienService {

    private static final int SLOT_MINUTES = 60;

    private final EntretienRepository entretienRepository;
    private final CandidatureClient candidatureClient;
    private final EntretienNotificationPublisher notificationPublisher;
    private final Clock clock;

    public EntretienServiceImpl(
            EntretienRepository entretienRepository,
            CandidatureClient candidatureClient,
            EntretienNotificationPublisher notificationPublisher,
            Clock clock) {
        this.entretienRepository   = entretienRepository;
        this.candidatureClient     = candidatureClient;
        this.notificationPublisher = notificationPublisher;
        this.clock                 = clock;
    }

    @Override
    public Entretien create(CreateEntretienRequest request) {
        log.info("[ENTRETIEN] Creation: candidatureId={}, recruteurId={}, dateHeure={}",
                request.getCandidatureId(), request.getRecruteurId(), request.getDateHeure());

        validateRequest(request);
        CandidatureSnapshot candidature = loadCandidature(request.getCandidatureId());

        if (entretienRepository.existsByCandidatureIdAndStatus(
                request.getCandidatureId(), InterviewStatus.PLANIFIE)) {
            log.warn("[ENTRETIEN] Conflit: entretien deja planifie pour candidatureId={}", request.getCandidatureId());
            throw new IllegalArgumentException("Un entretien est deja planifie pour cette candidature");
        }

        LocalDateTime start = request.getDateHeure().minusMinutes(SLOT_MINUTES - 1L);
        LocalDateTime end   = request.getDateHeure().plusMinutes(SLOT_MINUTES - 1L);

        if (entretienRepository.existsByRecruteurIdAndStatusAndDateHeureBetween(
                request.getRecruteurId(), InterviewStatus.PLANIFIE, start, end)) {
            log.warn("[ENTRETIEN] Conflit de creneau pour recruteurId={}", request.getRecruteurId());
            throw new IllegalArgumentException("Le recruteur a deja un entretien sur ce creneau");
        }
        if (entretienRepository.existsByCandidatIdAndStatusAndDateHeureBetween(
                candidature.getCandidatId(), InterviewStatus.PLANIFIE, start, end)) {
            log.warn("[ENTRETIEN] Conflit de creneau pour candidatId={}", candidature.getCandidatId());
            throw new IllegalArgumentException("Le candidat a deja un entretien sur ce creneau");
        }

        Entretien entretien = new Entretien();
        entretien.setCandidatureId(request.getCandidatureId());
        entretien.setCandidatId(candidature.getCandidatId());
        entretien.setRecruteurId(request.getRecruteurId());
        entretien.setDateHeure(request.getDateHeure());
        entretien.setLieu(request.getLieu());
        entretien.setLienVisio(request.getLienVisio());
        entretien.setType(request.getType());
        entretien.setNotesInternes(request.getNotesInternes());
        entretien.setStatus(InterviewStatus.PLANIFIE);

        Entretien saved = entretienRepository.save(entretien);
        try {
            candidatureClient.updateStatus(saved.getCandidatureId(), CandidatureStatus.ENTRETIEN.name());
        } catch (FeignException.Forbidden e) {
            throw new SecurityException("Accès refusé pour mettre à jour la candidature");
        } catch (FeignException.BadRequest e) {
            throw new IllegalArgumentException(
                    "Impossible de passer la candidature au statut Entretien — vérifiez qu'elle est bien « En cours ».");
        } catch (FeignException e) {
            log.warn("[ENTRETIEN] Mise à jour statut candidature {} : {}", saved.getCandidatureId(), e.getMessage());
            throw new IllegalArgumentException("Service candidatures indisponible pour finaliser l'entretien");
        }
        try {
            notificationPublisher.publish(saved, false);
        } catch (Exception e) {
            log.warn("[ENTRETIEN] Notification email non envoyée (entretien créé quand même) : {}", e.getMessage());
        }
        log.info("[ENTRETIEN] Cree avec succes: entretienId={}, candidatId={}", saved.getId(), saved.getCandidatId());
        return saved;
    }

    @Override
    public List<Entretien> listByCandidature(String candidatureId) {
        if (!StringUtils.hasText(candidatureId))
            throw new IllegalArgumentException("candidatureId est obligatoire");
        return entretienRepository.findByCandidatureIdOrderByDateHeureDesc(candidatureId);
    }

    @Override
    public List<Entretien> listByRecruteur(String recruteurId) {
        if (!StringUtils.hasText(recruteurId))
            throw new IllegalArgumentException("recruteurId est obligatoire");
        return entretienRepository.findByRecruteurIdOrderByDateHeureAsc(recruteurId);
    }

    @Override
    public List<Entretien> listByCandidat(String candidatId) {
        if (!StringUtils.hasText(candidatId))
            throw new IllegalArgumentException("candidatId est obligatoire");
        return entretienRepository.findByCandidatIdOrderByDateHeureAsc(candidatId);
    }

    @Override
    public Page<Entretien> listAdmin(InterviewStatus status, EntretienType type, String recruteurId,
                                     LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return entretienRepository.findAll(adminSpecification(status, type, recruteurId, from, to), pageable);
    }

    @Override
    public EntretienAdminStats adminStats() {
        long total = entretienRepository.count();
        long planifies = entretienRepository.countByStatus(InterviewStatus.PLANIFIE);
        long annules = entretienRepository.countByStatus(InterviewStatus.ANNULE);
        return new EntretienAdminStats(total, planifies, annules);
    }

    @Override
    public Entretien cancel(String entretienId, String recruteurId) {
        log.info("[ENTRETIEN] Annulation: entretienId={}, recruteurId={}", entretienId, recruteurId);

        if (!StringUtils.hasText(recruteurId))
            throw new IllegalArgumentException("recruteurId est obligatoire");

        UUID entretienUuid = parseEntretienId(entretienId);
        Entretien entretien = entretienRepository.findById(entretienUuid)
                .orElseThrow(() -> {
                    log.warn("[ENTRETIEN] Entretien non trouve: id={}", entretienId);
                    return new IllegalArgumentException("Entretien non trouve");
                });

        if (!recruteurId.equals(entretien.getRecruteurId()))
            throw new SecurityException("Le recruteur ne peut pas annuler cet entretien");

        if (entretien.getStatus() == InterviewStatus.ANNULE) {
            log.warn("[ENTRETIEN] Deja annule: entretienId={}", entretienId);
            return entretien;
        }

        entretien.setStatus(InterviewStatus.ANNULE);
        entretien.setDateAnnulation(LocalDateTime.now(clock));
        Entretien saved = entretienRepository.save(entretien);
        try {
            notificationPublisher.publish(saved, true);
        } catch (Exception e) {
            log.warn("[ENTRETIEN] Notification annulation non envoyée : {}", e.getMessage());
        }
        log.info("[ENTRETIEN] Annule avec succes: entretienId={}", saved.getId());
        return saved;
    }

    private Specification<Entretien> adminSpecification(InterviewStatus status, EntretienType type,
                                                        String recruteurId, LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (StringUtils.hasText(recruteurId)) {
                predicates.add(cb.equal(root.get("recruteurId"), recruteurId.trim()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateHeure"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateHeure"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validateRequest(CreateEntretienRequest request) {
        if (request == null)
            throw new IllegalArgumentException("La requete est obligatoire");
        if (!StringUtils.hasText(request.getCandidatureId()))
            throw new IllegalArgumentException("candidatureId est obligatoire");
        if (!StringUtils.hasText(request.getRecruteurId()))
            throw new IllegalArgumentException("recruteurId est obligatoire");
        if (request.getDateHeure() == null || !request.getDateHeure().isAfter(LocalDateTime.now(clock)))
            throw new IllegalArgumentException("La date de l'entretien doit etre dans le futur");
        if (request.getType() == null)
            throw new IllegalArgumentException("type est obligatoire");
        if (request.getType() == EntretienType.PRESENTIEL && !StringUtils.hasText(request.getLieu()))
            throw new IllegalArgumentException("Le lieu est obligatoire pour un entretien presentiel");
        if (request.getType() == EntretienType.VISIO && !StringUtils.hasText(request.getLienVisio()))
            throw new IllegalArgumentException("Le lien visio est obligatoire pour un entretien visio");
    }

    private CandidatureSnapshot loadCandidature(String candidatureId) {
        ApiResponse<CandidatureSnapshot> response;
        try {
            response = candidatureClient.getCandidatureById(candidatureId);
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Candidature inexistante");
        } catch (FeignException e) {
            log.warn("[ENTRETIEN] candidature-service indisponible pour {} : {}", candidatureId, e.getMessage());
            throw new IllegalArgumentException("Service candidatures indisponible");
        }
        if (response == null || !response.isSuccess() || response.getData() == null)
            throw new IllegalArgumentException("Candidature inexistante");
        return response.getData();
    }

    private static UUID parseEntretienId(String entretienId) {
        if (!StringUtils.hasText(entretienId)) {
            throw new IllegalArgumentException("entretienId est obligatoire");
        }
        try {
            return UUID.fromString(entretienId.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Identifiant entretien invalide");
        }
    }
}