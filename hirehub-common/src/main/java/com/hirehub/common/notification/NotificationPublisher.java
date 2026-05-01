package com.hirehub.common.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEmailEvent(String eventType, String email, String name,
                                  String routingKey, Map<String, Object> payload) {
        EmailEventDTO event = new EmailEventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setRecipientEmail(email);
        event.setRecipientName(name);
        event.setPayload(payload);

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                routingKey,
                event
        );
    }
}
