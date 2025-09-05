package com.loopers.interfaces.metrics;

import com.loopers.application.metrics.MetricsCriteria;
import com.loopers.application.metrics.MetricsFacade;
import com.loopers.application.metrics.MetricsResult;
import com.loopers.event.order.OrderKafkaEvent;
import com.loopers.event.like.LikeKafkaEvent;
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
public class MetricsConsumer {

    private final MetricsFacade metricsFacade;

    @KafkaListener(
        topics = "order-events",
        groupId = "metrics-group",
        containerFactory = "BATCH_LISTENER_DEFAULT"
    )
    public void handleOrderEvents(
            @Payload OrderKafkaEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("주문 메트릭 수신 - eventId: {}, eventType: {}, orderId: {}, topic: {}, partition: {}, offset: {}",
                    event.getEventId(), event.getEventType(), event.getOrderId(), topic, partition, offset);

            MetricsCriteria criteria = MetricsCriteria.of(event, topic, partition, offset);

            
            MetricsResult result = metricsFacade.processOrderMetrics(criteria);

            if (result.isSuccess()) {
                log.info("주문 메트릭 처리 성공 - eventId: {}, eventType: {}, processedCount: {}, message: {}",
                        result.getEventId(), result.getEventType(), result.getProcessedCount(), result.getMessage());
            } else {
                log.error("주문 메트릭 처리 실패 - eventId: {}, eventType: {}, error: {}",
                        result.getEventId(), result.getEventType(), result.getErrorMessage());
            }

            acknowledgment.acknowledge(); 

        } catch (Exception e) {
            log.error("주문 메트릭 처리 중 오류 발생 - eventId: {}, error: {}",
                    event.getEventId(), e.getMessage(), e);

            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
        topics = "like-events",
        groupId = "metrics-group",
        containerFactory = "BATCH_LISTENER_DEFAULT"
    )
    public void handleLikeEvents(
            @Payload LikeKafkaEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("좋아요 메트릭 수신 - eventId: {}, eventType: {}, userId: {}, productId: {}, topic: {}, partition: {}, offset: {}",
                    event.getEventId(), event.getEventType(), event.getUserId(), event.getProductId(), topic, partition, offset);

            MetricsCriteria criteria = MetricsCriteria.of(event, topic, partition, offset);

            
            MetricsResult result = metricsFacade.processLikeMetrics(criteria);

            if (result.isSuccess()) {
                log.info("좋아요 메트릭 처리 성공 - eventId: {}, eventType: {}, processedCount: {}, message: {}",
                        result.getEventId(), result.getEventType(), result.getProcessedCount(), result.getMessage());
            } else {
                log.error("좋아요 메트릭 처리 실패 - eventId: {}, eventType: {}, error: {}",
                        result.getEventId(), result.getEventType(), result.getErrorMessage());
            }

            acknowledgment.acknowledge(); 

        } catch (Exception e) {
            log.error("좋아요 메트릭 처리 중 오류 발생 - eventId: {}, error: {}",
                    event.getEventId(), e.getMessage(), e);

            acknowledgment.acknowledge();
        }
    }
}
