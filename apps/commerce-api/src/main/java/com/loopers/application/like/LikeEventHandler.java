package com.loopers.application.like;

import com.loopers.domain.like.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventHandler {
    
    // 좋아요 생성 이벤트 - 동기 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeCreated(LikeEvent.Created event) {
        log.info("좋아요 생성 이벤트 처리 시작 - likeId: {}, userId: {}, productId: {}", 
                event.likeId(), event.userId(), event.productId());
        
        try {
            
            log.info("좋아요 생성 이벤트 처리 완료 - likeId: {}", event.likeId());
            
        } catch (Exception e) {
            log.error("좋아요 생성 이벤트 처리 중 오류 발생 - likeId: {}, error: {}", 
                    event.likeId(), e.getMessage());
        }
    }
    
    // 좋아요 삭제
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeRemoved(LikeEvent.Removed event) {
        log.info("좋아요 삭제 이벤트 처리 시작 - likeId: {}, userId: {}, productId: {}", 
                event.likeId(), event.userId(), event.productId());
        
        try {
            log.info("좋아요 삭제 이벤트 처리 완료 - likeId: {}", event.likeId());
            
        } catch (Exception e) {
            log.error("좋아요 삭제 이벤트 처리 중 오류 발생 - likeId: {}, error: {}", 
                    event.likeId(), e.getMessage());
        }
    }
    
    // 집계 처리 이벤트 - 비동기
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleAggregationRequested(LikeEvent.AggregationRequested event) {
        log.info("좋아요 집계 처리 이벤트 시작 - productId: {}, currentLikes: {}", 
                event.productId(), event.currentLikes());
        
        try {
            // TODO: 백그라운드에서 집계 데이터 정리 (비동기)
            // likeAggregationService.processAggregation(event.productId());
            
            // TODO: 캐시 무효화 (비동기)
            // productCacheService.evictProductCache(event.productId());
            
            // TODO: 분석 데이터 전송 (비동기)
            // analyticsService.sendLikeAnalytics(event.productId(), event.currentLikes());
            
            log.info("좋아요 집계 처리 이벤트 완료 - productId: {}", event.productId());
            
        } catch (Exception e) {
            log.error("좋아요 집계 처리 이벤트 중 오류 발생 - productId: {}, error: {}", 
                    event.productId(), e.getMessage());
        }
    }
}
