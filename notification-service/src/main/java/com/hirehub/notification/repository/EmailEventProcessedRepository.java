package com.hirehub.notification.repository;

import com.hirehub.notification.entity.EmailEventProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository pour la table email_events_processed.
 * Permet de gérer l'idempotence et le suivi des événements traités.
 */
@Repository
public interface EmailEventProcessedRepository extends JpaRepository<EmailEventProcessed, Long> {

    /**
     * Cherche un événement par son ID unique.
     * @param eventId UUID de l'événement
     * @return Optional contenant l'entité si trouvée
     */
    Optional<EmailEventProcessed> findByEventId(String eventId);

    /**
     * Compte le nombre d'événements déjà traités pour un destinataire et un type donné.
     * @param recipientEmail Email du destinataire
     * @param eventType Type d'événement
     * @return Nombre d'événements traités
     */
    Long countByRecipientEmailAndEventType(String recipientEmail, String eventType);

    /**
     * Supprime les événements plus anciens que la date donnée (nettoyage).
     * @param dateThreshold Date limite
     * @return Nombre de lignes supprimées
     */
    @Query("DELETE FROM EmailEventProcessed e WHERE e.createdAt < :dateThreshold")
    Long deleteOlderThan(@Param("dateThreshold") LocalDateTime dateThreshold);

}

