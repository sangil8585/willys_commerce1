package com.loopers.interfaces.eventlog;

import com.loopers.application.eventlog.EventLogCriteria;
import com.loopers.application.eventlog.EventLogFacade;
import com.loopers.application.eventlog.EventLogResult;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventLogConsumer {

    private final EventLogFacade eventLogFacade;

    @KafkaListener(
        topics = {"order-events", "like-events", "payment-events"},
        groupId = "audit-log-group",
        containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleAllEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("이벤트 로그 수신 - eventId: {}, eventType: {}, topic: {}, partition: {}, offset: {}",
                    event.getEventId(), event.getEventType(), topic, partition, offset);

            EventLogCriteria criteria = EventLogCriteria.of(event, topic, partition, offset);

            EventLogResult result = eventLogFacade.processEventLog(criteria);

            if (result.isSuccess()) {
                log.info("이벤트 로그 처리 성공 - eventId: {}, eventType: {}, message: {}",
                        result.getEventId(), result.getEventType(), result.getMessage());
            } else {
                log.error("이벤트 로그 처리 실패 - eventId: {}, eventType: {}, error: {}",
                        result.getEventId(), result.getEventType(), result.getErrorMessage());
            }

            acknowledgment.acknowledge(); 

        } catch (Exception e) {
            log.error("이벤트 로그 처리 중 오류 발생 - eventId: {}, error: {}",
                    event.getEventId(), e.getMessage(), e);

            acknowledgment.acknowledge();
        }
    }
}
