package com.hirehub.frontend.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventLogFrontendClient {

    private static final Logger log = LoggerFactory.getLogger(EventLogFrontendClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String eventBaseUrl;

    public EventLogFrontendClient(@Value("${hirehub.event-service-base-url}") String eventBaseUrl) {
        this.eventBaseUrl = eventBaseUrl;
    }

    public List<EventAuditView> listRecent(String eventType, String source, int limit) {
        try {
            EventAuditView[] logs = restTemplate.getForObject(eventBaseUrl + "/api/event-logs", EventAuditView[].class);
            if (logs == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(logs)
                    .filter(e -> eventType == null || eventType.isBlank()
                            || (e.getEventType() != null && e.getEventType().contains(eventType)))
                    .filter(e -> source == null || source.isBlank()
                            || (e.getSourceService() != null && e.getSourceService().contains(source)))
                    .limit(Math.max(1, limit))
                    .collect(Collectors.toList());
        } catch (RestClientException ex) {
            log.warn("Event logs API: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}
