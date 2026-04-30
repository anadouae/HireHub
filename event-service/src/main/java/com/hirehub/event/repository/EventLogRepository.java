package com.hirehub.event.repository;

import com.hirehub.event.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    EventLog findByEventId(String eventId);

    List<EventLog> findByEventType(String eventType);

    List<EventLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EventLog> findByStatus(String status);
}
