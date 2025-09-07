package com.loopers.application.eventlog;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventLogResult {
    
    private final boolean success;
    private final String eventId;
    private final String eventType;
    private final String message;
    private final String errorMessage;
    
    public static EventLogResult success(String eventId, String eventType, String message) {
        return EventLogResult.builder()
                .success(true)
                .eventId(eventId)
                .eventType(eventType)
                .message(message)
                .build();
    }
    
    public static EventLogResult failure(String eventId, String eventType, String errorMessage) {
        return EventLogResult.builder()
                .success(false)
                .eventId(eventId)
                .eventType(eventType)
                .errorMessage(errorMessage)
                .build();
    }
}
