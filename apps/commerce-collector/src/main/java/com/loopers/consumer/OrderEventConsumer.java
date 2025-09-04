package com.loopers.consumer;

import com.loopers.event.order.OrderEvent;
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
public class OrderEventConsumer {
    
    @KafkaListener(
        topics = "order-events",
        groupId = "commerce-collector-group",
        containerFactory = "BATCH_LISTENER_DEFAULT"
    )
    public void handleOrderEvents(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("주문 이벤트 수신 - eventId: {}, eventType: {}, orderId: {}, userId: {}, topic: {}, partition: {}, offset: {}", 
                    event.getEventId(), event.getEventType(), event.getOrderId(), event.getUserId(), topic, partition, offset);
            
            // 이벤트 타입에 따른 처리
            switch (event.getEventType()) {
                case "OrderCompleted" -> handleOrderCompleted(event);
                case "PaymentCompleted" -> handlePaymentCompleted(event);
                default -> log.warn("알 수 없는 이벤트 타입: {}", event.getEventType());
            }
            
            // 수동 커밋
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("주문 이벤트 처리 중 오류 발생 - eventId: {}, error: {}", 
                    event.getEventId(), e.getMessage(), e);
            // 에러 발생 시에도 커밋 (재시도 정책에 따라 처리)
            acknowledgment.acknowledge();
        }
    }
    
    private void handleOrderCompleted(OrderEvent event) {
        log.info("주문 완료 이벤트 처리 - orderId: {}, userId: {}, 총액: {}, 할인: {}", 
                event.getOrderId(), event.getUserId(), event.getTotalAmount(), event.getDiscountAmount());
        
        // TODO: 주문 완료 관련 비즈니스 로직 구현
        // - 주문 통계 업데이트
        // - 재고 모니터링
        // - 사용자 행동 분석
        // - 알림 발송 등
    }
    
    private void handlePaymentCompleted(OrderEvent event) {
        log.info("결제 완료 이벤트 처리 - orderId: {}, userId: {}, 최종금액: {}", 
                event.getOrderId(), event.getUserId(), event.getTotalAmount());
        
        // TODO: 결제 완료 관련 비즈니스 로직 구현
        // - 결제 통계 업데이트
        // - 매출 분석
        // - 고객 세분화
        // - 마케팅 이벤트 트리거 등
    }
}
