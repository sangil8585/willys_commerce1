package com.loopers.application.eventlog;

import com.loopers.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventLogCriteria {
    
    private final BaseEvent event;
    private final String topic;
    private final int partition;
    private final long offset;
    
    public static EventLogCriteria of(BaseEvent event, String topic, int partition, long offset) {
        return EventLogCriteria.builder()
                .event(event)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .build();
    }
}
