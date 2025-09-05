package com.loopers.application.metrics;

import com.loopers.event.BaseEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MetricsCriteria {
    
    private final BaseEvent event;
    private final String topic;
    private final int partition;
    private final long offset;
    private final LocalDate date;
    
    public static MetricsCriteria of(BaseEvent event, String topic, int partition, long offset) {
        return MetricsCriteria.builder()
                .event(event)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .date(LocalDate.now())
                .build();
    }
    
    public static MetricsCriteria of(BaseEvent event, String topic, int partition, long offset, LocalDate date) {
        return MetricsCriteria.builder()
                .event(event)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .date(date)
                .build();
    }
}
