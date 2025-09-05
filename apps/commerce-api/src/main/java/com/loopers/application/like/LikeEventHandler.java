package com.loopers.application.like;

import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.config.kafka.KafkaEventPublisher;
import com.loopers.event.like.LikeKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventHandler {
    
    private final LikeService likeService;
    private final ProductService productService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleLikeCreated(LikeEvent.Created event) {
        try {
            LikeCommand.Create command = new LikeCommand.Create(event.userId(), event.productId());
            LikeEntity likeEntity = likeService.createLike(command);
            
            // 상품의 좋아요 카운트 증가
            productService.findById(event.productId()).ifPresent(product -> {
                product.incrementLikes();
                productService.save(product);
            });
            
            log.info("좋아요 생성 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
        } catch (CoreException e) {
            log.error("좋아요 생성 이벤트 처리 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeCreatedAfterCommit(LikeEvent.Created event) {
        try {
            // Kafka로 좋아요 생성 이벤트 발행
            LikeKafkaEvent kafkaEvent = LikeKafkaEvent.likeCreated(event.userId(), event.productId());
            kafkaEventPublisher.publishEventAsync("like-events", kafkaEvent);
            
            log.info("좋아요 생성 이벤트 발행 완료 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
        } catch (Exception e) {
            log.error("좋아요 생성 이벤트 발행 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
        }
    }
    
    // 좋아요 삭제 이벤트 - 좋아요 처리만 담당
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleLikeRemoved(LikeEvent.Removed event) {
        log.info("좋아요 삭제 이벤트 처리 시작 - userId: {}, productId: {}", 
                event.userId(), event.productId());
        
        try {
            // 실제 좋아요 삭제만 처리
            likeService.removeLike(event.userId(), event.productId());
            
            // 상품의 좋아요 카운트 감소
            productService.findById(event.productId()).ifPresent(product -> {
                product.decrementLikes();
                productService.save(product);
            });
            
            log.info("좋아요 삭제 완료 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
            
        } catch (Exception e) {
            log.error("좋아요 삭제 이벤트 처리 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeRemovedAfterCommit(LikeEvent.Removed event) {
        try {
            // Kafka로 좋아요 삭제 이벤트 발행
            LikeKafkaEvent kafkaEvent = LikeKafkaEvent.likeRemoved(event.userId(), event.productId());
            kafkaEventPublisher.publishEventAsync("like-events", kafkaEvent);
            
            log.info("좋아요 삭제 이벤트 발행 완료 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
        } catch (Exception e) {
            log.error("좋아요 삭제 이벤트 발행 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
        }
    }
}
