package com.hirehub.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║           Configuration Beans - Notification Service          ║
 * ║                                                                ║
 * ║  Déclare les beans partagés utilisés dans le service.        ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Configuration
public class NotificationConfig {

    /**
     * ObjectMapper pour la sérialisation/désérialisation JSON
     * Utilisé par le QueueConsumer pour parser les messages RabbitMQ
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

