package com.loopers.application.order;

import com.loopers.domain.order.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCreated(OrderEvent.Completed event) {
        log.info("주문 생성 완료 이벤트 처리 시작 - orderId: {}, userId: {}, 총액: {}, 할인: {}", 
                event.orderId(), event.userId(), event.totalAmount(), event.discountAmount());
        
        try {
            // TODO: 데이터 플랫폼 전송 (부가 기능)
            // dataPlatformService.sendOrderData(event.orderId());
            
            // TODO: 쿠폰 히스토리 기록 (부가 기능)
            // couponHistoryService.recordUsage(event.userId(), event.orderId());
            
            // TODO: 포인트 히스토리 기록 (부가 기능)
            // pointHistoryService.recordUsage(event.userId(), event.orderId());
            
            log.info("주문 생성 완료 이벤트 처리 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("주문 생성 완료 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
            // 이벤트 처리 실패 시에도 주문 생성은 계속 진행 (비동기 재시도 가능)
        }
    }
}
