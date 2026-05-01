package com.hirehub.notification.service;

import com.hirehub.notification.entity.EmailEventProcessed;
import com.hirehub.notification.repository.EmailEventProcessedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service pour gérer l'idempotence des événements d'email.
 * Empêche l'envoi en double d'emails en cas de retry RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotenceService {

    private final EmailEventProcessedRepository repository;
    private static final int TTL_DAYS = 30; // Garder l'historique 30 jours

    /**
     * Vérifie si un événement a déjà été traité.
     * @param eventId UUID unique de l'événement
     * @return true si l'événement a déjà été traité, false sinon
     */
    public boolean isAlreadyProcessed(String eventId) {
        Optional<EmailEventProcessed> existing = repository.findByEventId(eventId);
        if (existing.isPresent()) {
            log.warn("[IDEMPOTENCE] Événement {} déjà traité", eventId);
            return true;
        }
        return false;
    }

    /**
     * Marque un événement comme traité avec succès.
     * @param eventId UUID unique de l'événement
     * @param eventType Type d'événement
     * @param recipientEmail Email du destinataire
     */
    @Transactional
    public void markAsProcessed(String eventId, String eventType, String recipientEmail) {
        EmailEventProcessed event = EmailEventProcessed.builder()
                .eventId(eventId)
                .eventType(eventType)
                .recipientEmail(recipientEmail)
                .status("SUCCESS")
                .processedAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        repository.save(event);
        log.info("[IDEMPOTENCE] Événement {} marqué comme traité", eventId);
    }

    /**
     * Marque un événement comme échoué avec un message d'erreur.
     * @param eventId UUID unique de l'événement
     * @param eventType Type d'événement
     * @param recipientEmail Email du destinataire
     * @param errorMessage Message d'erreur
     */
    @Transactional
    public void markAsFailed(String eventId, String eventType, String recipientEmail, String errorMessage) {
        Optional<EmailEventProcessed> existing = repository.findByEventId(eventId);

        EmailEventProcessed event;
        if (existing.isPresent()) {
            event = existing.get();
            event.setRetryCount(event.getRetryCount() + 1);
            event.setStatus("RETRY");
        } else {
            event = EmailEventProcessed.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .recipientEmail(recipientEmail)
                    .status("FAILED")
                    .retryCount(1)
                    .build();
        }

        event.setErrorMessage(errorMessage);
        event.setProcessedAt(LocalDateTime.now());
        repository.save(event);
        log.error("[IDEMPOTENCE] Événement {} marqué comme échoué: {}", eventId, errorMessage);
    }

    /**
     * Nettoie les anciens événements (plus vieux que TTL_DAYS).
     * À exécuter périodiquement (ex: tous les jours à minuit).
     */
    @Scheduled(cron = "0 0 0 * * *") // Tous les jours à 00:00
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(TTL_DAYS);
        Long deletedCount = repository.deleteOlderThan(threshold);
        log.info("[IDEMPOTENCE] Nettoyage: {} événements supprimés (plus de {} jours)", deletedCount, TTL_DAYS);
    }

}

