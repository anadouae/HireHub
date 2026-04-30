package com.hirehub.common.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration JSON commune pour RabbitMQ.
 *
 * Objectif: forcer une sérialisation/désérialisation JSON identique dans tous les microservices,
 * afin que les publishers (RabbitTemplate) et les consumers (@RabbitListener) parlent le même contrat.
 */
@Configuration
public class RabbitMQJsonConfig {

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

