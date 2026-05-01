package com.hirehub.candidature.services;

import com.hirehub.common.constants.EventType;
import com.hirehub.common.notification.NotificationPublisher;
import com.hirehub.common.notification.RabbitMQConstants;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CandidatureEventPublisher {

    private final NotificationPublisher notificationPublisher;

    public CandidatureEventPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    /**
     * Publie l'événement candidature.created destiné à notification-service.
     *
     * Contrat: on publie un EmailEventDTO (JSON) afin que notification-service puisse le désérialiser.
     * L'audit peut être fait via le même DTO, ou via une routing key dédiée si vous voulez séparer.
     */
    public void publishCandidatureCreated(String candidatureId,
                                         String candidateEmail,
                                         String candidateName,
                                         String offerId,
                                         String offerTitle) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("candidatureId", candidatureId);
        payload.put("offerId", offerId);
        payload.put("offerTitle", offerTitle);

        notificationPublisher.publishEmailEvent(
                EventType.CANDIDATURE_CREATED,
                candidateEmail,
                candidateName,
                RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
                payload
        );
    }
}
