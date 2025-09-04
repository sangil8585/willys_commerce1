package com.loopers.config.kafka;

import com.loopers.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishEvent(String topic, String key, BaseEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("이벤트 발행 성공 - topic: {}, key: {}, eventId: {}, eventType: {}", 
                            topic, key, event.getEventId(), event.getEventType());
                } else {
                    log.error("이벤트 발행 실패 - topic: {}, key: {}, eventId: {}, error: {}", 
                            topic, key, event.getEventId(), ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            log.error("이벤트 발행 중 예외 발생 - topic: {}, key: {}, eventId: {}, error: {}", 
                    topic, key, event.getEventId(), e.getMessage(), e);
        }
    }
    
    public void publishEvent(String topic, BaseEvent event) {
        publishEvent(topic, event.getEventId(), event);
    }
}
