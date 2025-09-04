package com.loopers.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public abstract class BaseEvent {
    
    private String eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private String source;
    private String version;
    
    protected BaseEvent(String eventType, String source, String version) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
        this.source = source;
        this.version = version;
    }
    
    protected BaseEvent(String eventId, String eventType, LocalDateTime occurredAt, String source, String version) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.source = source;
        this.version = version;
    }
}
