package com.hirehub.event.service;

import com.hirehub.event.entity.EventLog;
import com.hirehub.event.repository.EventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Persiste un événement dans le log d'audit
     */
    public EventLog logEvent(String eventId, String eventType, String message,
                             String sourceService, String destinationService) {
        try {
            EventLog eventLog = EventLog.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .sourceService(sourceService)
                    .destinationService(destinationService)
                    .status("SUCCESS")
                    .build();

            EventLog savedLog = eventLogRepository.save(eventLog);
            log.debug("[EventLog] Événement persisté: {} ({})", eventType, eventId);
            return savedLog;

        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors de la persistance: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur persistance EventLog", e);
        }
    }

    /**
     * Persiste un événement avec un statut d'erreur
     */
    public EventLog logEventError(String eventId, String eventType, String message,
                                  String sourceService, String errorMessage) {
        try {
            EventLog eventLog = EventLog.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .sourceService(sourceService)
                    .status("FAILED")
                    .errorMessage(errorMessage)
                    .build();

            return eventLogRepository.save(eventLog);

        } catch (Exception e) {
            log.error("[EventLog ERROR] Erreur lors du log d'erreur: {}", e.getMessage(), e);
            // Ne pas relancer - on ne veut pas bloquer l'audit
            return null;
        }
    }
}
