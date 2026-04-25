package com.hirehub.candidature.services;

import com.hirehub.common.constants.EventType;
import com.hirehub.common.constants.RabbitMQConstants;
import com.hirehub.common.constants.ServiceName;
import com.hirehub.common.dtos.events.EventMessage;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CandidatureEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public CandidatureEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCandidatureCreated(Long candidatureId, String candidateEmail, String offerTitle) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("candidatureId", candidatureId);
        payload.put("candidateEmail", candidateEmail);
        payload.put("offerTitle", offerTitle);

        EventMessage message = new EventMessage(
                EventType.CANDIDATURE_CREATED,
                ServiceName.CANDIDATURE_SERVICE,
                ServiceName.NOTIFICATION_SERVICE,
                UUID.randomUUID().toString(),
                Instant.now(),
                payload
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                message
        );
    }
}
