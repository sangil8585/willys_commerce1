package com.loopers.consumer;

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
public class LikeEventConsumer {
    
    @KafkaListener(
        topics = "like-events",
        groupId = "commerce-collector-group",
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
            log.info("좋아요 이벤트 수신 - eventId: {}, eventType: {}, userId: {}, productId: {}, topic: {}, partition: {}, offset: {}", 
                    event.getEventId(), event.getEventType(), event.getUserId(), event.getProductId(), topic, partition, offset);
            
            // 이벤트 타입에 따른 처리
            switch (event.getEventType()) {
                case "LikeCreated" -> handleLikeCreated(event);
                case "LikeRemoved" -> handleLikeRemoved(event);
                default -> log.warn("알 수 없는 이벤트 타입: {}", event.getEventType());
            }
            
            // 수동 커밋
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("좋아요 이벤트 처리 중 오류 발생 - eventId: {}, error: {}", 
                    event.getEventId(), e.getMessage(), e);
            // 에러 발생 시에도 커밋 (재시도 정책에 따라 처리)
            acknowledgment.acknowledge();
        }
    }
    
    private void handleLikeCreated(LikeKafkaEvent event) {
        log.info("좋아요 생성 이벤트 처리 - userId: {}, productId: {}", 
                event.getUserId(), event.getProductId());
        
        // TODO: 좋아요 생성 관련 비즈니스 로직 구현
        // - 상품 인기도 분석
        // - 사용자 선호도 분석
        // - 추천 시스템 업데이트
        // - 실시간 통계 업데이트 등
    }
    
    private void handleLikeRemoved(LikeKafkaEvent event) {
        log.info("좋아요 삭제 이벤트 처리 - userId: {}, productId: {}", 
                event.getUserId(), event.getProductId());
        
        // TODO: 좋아요 삭제 관련 비즈니스 로직 구현
        // - 상품 인기도 재계산
        // - 사용자 선호도 업데이트
        // - 추천 시스템 재조정 등
    }
}
