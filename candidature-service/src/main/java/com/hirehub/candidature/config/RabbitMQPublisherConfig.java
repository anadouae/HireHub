package com.hirehub.candidature.config;

import com.hirehub.common.constants.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le service candidature
 * Ce service PUBLIE des événements (ne consomme pas)
 */
@Configuration
@Slf4j
public class RabbitMQPublisherConfig {

    /**
     * Définit l'exchange global utilisé par tous les services
     * Note : Cet exchange est créé lors du démarrage du notification-service
     * Mais on le redéfinit ici pour être sûr qu'il existe
     */
    @Bean
    public TopicExchange hirehubExchange() {
        log.info("[CANDIDATURE] Configuration de l'exchange RabbitMQ: {}", RabbitMQConstants.EXCHANGE);
        return new TopicExchange(
                RabbitMQConstants.EXCHANGE,
                true,   // durable
                false   // autoDelete
        );
    }
}

