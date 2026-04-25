package com.hirehub.common.dtos.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/***
 * DTO de message pour les evenmenet
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventMessage {
    private String eventType;
    private String sourceService;
    private String targetService;
    private String correlationId;
    private Instant createdAt;
    private Map<String, Object> payload;

}
