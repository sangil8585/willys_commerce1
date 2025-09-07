package com.loopers.domain.eventlog;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.loopers.event.BaseEvent;

@Entity
@Table(name = "event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "payload", columnDefinition = "JSON")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public EventLogEntity(String eventId, String eventType, LocalDateTime occurredAt, 
                         String source, String version, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.source = source;
        this.version = version;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

    public static EventLogEntity from(BaseEvent event) {
        return EventLogEntity.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .occurredAt(event.getOccurredAt())
                .source(event.getSource())
                .version(event.getVersion())
                .payload(convertToJson(event))
                .build();
    }

    private static String convertToJson(BaseEvent event) {
        // 간단한 JSON 변환 (실제로는 Jackson ObjectMapper 사용)
        return String.format("{\"eventId\":\"%s\",\"eventType\":\"%s\",\"occurredAt\":\"%s\",\"source\":\"%s\",\"version\":\"%s\"}",
                event.getEventId(), event.getEventType(), event.getOccurredAt(), event.getSource(), event.getVersion());
    }
}
