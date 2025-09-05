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
    

    public void publishEventAsync(String topic, String key, BaseEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("이벤트 발행 성공 - topic: {}, key: {}, eventId: {}, eventType: {}, partition: {}, offset: {}", 
                            topic, key, event.getEventId(), event.getEventType(),
                            result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("이벤트 발행 실패 - topic: {}, key: {}, eventId: {}, error: {}", 
                            topic, key, event.getEventId(), ex.getMessage());
                    // 비동기에서는 재시도하지 않음 (Kafka Producer 설정에서 자동 재시도)
                }
            });
            
        } catch (Exception e) {
            log.error("이벤트 발행 중 예외 발생 - topic: {}, key: {}, eventId: {}, error: {}", 
                    topic, key, event.getEventId(), e.getMessage(), e);
        }
    }
    
    public void publishEventAsync(String topic, BaseEvent event) {
        publishEventAsync(topic, event.getEventId(), event);
    }
}
