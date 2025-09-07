package com.loopers.application.metrics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetricsResult {
    
    private final boolean success;
    private final String eventId;
    private final String eventType;
    private final String message;
    private final String errorMessage;
    private final int processedCount;
    
    public static MetricsResult success(String eventId, String eventType, String message, int processedCount) {
        return MetricsResult.builder()
                .success(true)
                .eventId(eventId)
                .eventType(eventType)
                .message(message)
                .processedCount(processedCount)
                .build();
    }
    
    public static MetricsResult failure(String eventId, String eventType, String errorMessage) {
        return MetricsResult.builder()
                .success(false)
                .eventId(eventId)
                .eventType(eventType)
                .errorMessage(errorMessage)
                .processedCount(0)
                .build();
    }
}
