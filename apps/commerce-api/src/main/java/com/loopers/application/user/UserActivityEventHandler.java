package com.loopers.application.user;

import com.loopers.domain.user.UserActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityEventHandler {
    
    // 상품 조회 이벤트
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleProductViewed(UserActivityEvent.ProductViewed event) {
        log.info("상품 조회 이벤트 처리 시작 - userId: {}, productId: {}", 
                event.userId(), event.productId());
        
        try {
            log.info("상품 조회 이벤트 처리 완료 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
            
        } catch (Exception e) {
            log.error("상품 조회 이벤트 처리 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
        }
    }
    
    // 상품 클릭 이벤트
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleProductClicked(UserActivityEvent.ProductClicked event) {
        log.info("상품 클릭 이벤트 처리 시작 - userId: {}, productId: {}, clickType: {}", 
                event.userId(), event.productId(), event.clickType());
        
        try {
            log.info("상품 클릭 이벤트 처리 완료 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
            
        } catch (Exception e) {
            log.error("상품 클릭 이벤트 처리 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
        }
    }
    
    // 주문 이벤트
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderPlaced(UserActivityEvent.OrderPlaced event) {
        log.info("주문 이벤트 처리 시작 - userId: {}, orderId: {}, totalAmount: {}", 
                event.userId(), event.orderId(), event.totalAmount());
        
        try {
            log.info("주문 이벤트 처리 완료 - userId: {}, orderId: {}", 
                    event.userId(), event.orderId());
            
        } catch (Exception e) {
            log.error("주문 이벤트 처리 중 오류 발생 - userId: {}, orderId: {}, error: {}", 
                    event.userId(), event.orderId(), e.getMessage());
        }
    }
    
    // 좋아요 액션 이벤트
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleLikeAction(UserActivityEvent.LikeAction event) {
        log.info("좋아요 액션 이벤트 처리 시작 - userId: {}, productId: {}, actionType: {}", 
                event.userId(), event.productId(), event.actionType());
        
        try {
            log.info("좋아요 액션 이벤트 처리 완료 - userId: {}, productId: {}", 
                    event.userId(), event.productId());
            
        } catch (Exception e) {
            log.error("좋아요 액션 이벤트 처리 중 오류 발생 - userId: {}, productId: {}, error: {}", 
                    event.userId(), event.productId(), e.getMessage());
            // 이벤트 처리 실패 시에도 좋아요 액션은 정상적으로 처리 되도록...
        }
    }
}
