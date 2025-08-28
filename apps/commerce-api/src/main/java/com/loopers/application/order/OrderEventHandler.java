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
            
            log.info("주문 생성 완료 이벤트 처리 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("주문 생성 완료 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
            // 이벤트 처리 실패 시에도 주문 생성은 계속 진행 (비동기 재시도 가능)
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentCompleted(OrderEvent.PaymentCompleted event) {
        log.info("결제 완료 이벤트 처리 시작 - orderId: {}, paymentId: {}, 최종금액: {}", 
                event.orderId(), event.paymentId(), event.finalAmount());
        
        try {
            // TODO: 주문 상태 업데이트 (부가 기능)
            // orderService.updateOrderStatus(event.orderId(), OrderState.PAID);
            
            // TODO: 재고 차감 (부가 기능)
            // productService.deductStock(event.orderId());
            
            log.info("결제 완료 이벤트 처리 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
            // 이벤트 처리 실패 시에도 결제는 완료된 상태로 유지
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleDataPlatformSent(OrderEvent.DataPlatformSent event) {
        log.info("데이터 플랫폼 전송 이벤트 처리 시작 - orderId: {}, eventType: {}", 
                event.orderId(), event.eventType());
        
        try {
            // TODO: 데이터 플랫폼 전송 상태 업데이트 (부가 기능)
            // dataPlatformService.updateSendStatus(event.orderId(), "SENT");
            
            log.info("데이터 플랫폼 전송 이벤트 처리 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
            // 이벤트 처리 실패 시에도 메인 트랜잭션은 영향받지 않음
        }
    }
}
