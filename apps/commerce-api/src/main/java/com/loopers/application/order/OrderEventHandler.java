package com.loopers.application.order;

import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderEntity;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {
    
    private final CouponService couponService;
    private final ProductService productService;
    private final OrderService orderService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCreated(OrderEvent.Completed event) {
        log.info("주문 생성 완료 이벤트 처리 시작 - orderId: {}, userId: {}, 총액: {}, 할인: {}", 
                event.orderId(), event.userId(), event.totalAmount(), event.discountAmount());
        
        try {
            // - 주문 통계 업데이트
            // - 재고 모니터링 정도 할수있을듯..
            
            log.info("주문 생성 완료 이벤트 처리 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("주문 생성 완료 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
            // 이벤트 처리 실패 시에도 주문 생성은 계속 진행 (비동기 재시도 가능)
        }
    }
    
    // BEFORE_COMMIT으로 변경
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCouponUsageRequested(OrderEvent.CouponUsageRequested event) {
        log.info("쿠폰 사용 요청 이벤트 처리 시작 - orderId: {}, couponId: {}, 주문금액: {}", 
                event.orderId(), event.couponId(), event.orderAmount());
        
        try {
            // 쿠폰 사용 처리
            Long discountAmount = couponService.calculateDiscount(event.couponId(), event.userId(), event.orderAmount());
            couponService.useCoupon(event.couponId(), event.userId(), event.orderAmount());
            
            Long finalAmount = event.orderAmount() - discountAmount;
            
            log.info("쿠폰 사용 완료 - orderId: {}, couponId: {}, 할인금액: {}, 최종금액: {}", 
                    event.orderId(), event.couponId(), discountAmount, finalAmount);
        } catch (Exception e) {
            log.error("쿠폰 사용 이벤트 처리 중 오류 발생 - orderId: {}, couponId: {}, error: {}", 
                    event.orderId(), event.couponId(), e.getMessage());
            // 🚨 BEFORE_COMMIT이므로 예외를 다시 던져서 주문 트랜잭션을 롤백시킴
            throw new RuntimeException("쿠폰 사용 처리 실패: " + e.getMessage(), e);
        }
    }
    
    // BEFORE_COMMIT으로 변경
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleStockReservationRequested(OrderEvent.StockReservationRequested event) {
        log.info("재고 예약 요청 이벤트 처리 시작 - orderId: {}, userId: {}, items: {}", 
                event.orderId(), event.userId(), event.items());
        
        try {
            // 재고 예약 처리 (실제 재고 차감은 결제 완료 후)
            Map<Long, Integer> stockReservationMap = event.items().stream()
                    .collect(Collectors.toMap(
                            OrderEvent.StockReservationItem::productId,
                            OrderEvent.StockReservationItem::quantity
                    ));
            
            // 재고 예약 (제고 차감은 하지 않고 예약만)
            productService.reserveStock(stockReservationMap);
            
            log.info("재고 예약 완료 - orderId: {}, items: {}", event.orderId(), event.items());
            
        } catch (Exception e) {
            log.error("재고 예약 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
            // 예외를 다시 던져서 주문 트랜잭션을 롤백 하도록 변경
            throw new RuntimeException("재고 예약 처리 실패: " + e.getMessage(), e);
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentCompleted(OrderEvent.PaymentCompleted event) {
        log.info("결제 완료 이벤트 처리 시작 - orderId: {}, paymentId: {}, 최종금액: {}", 
                event.orderId(), event.paymentId(), event.finalAmount());
        
        try {
            // 실제 재고 차감 (결제 완료 후)
            OrderEntity order = orderService.findById(event.orderId())
                    .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다: " + event.orderId()));
            
            Map<Long, Integer> stockDeductionMap = order.getItems().stream()
                    .collect(Collectors.toMap(
                            item -> item.getProductId(),
                            item -> item.getQuantity().intValue()
                    ));
            productService.deductStock(stockDeductionMap);
            
            log.info("결제 완료 후 재고 차감 완료 - orderId: {}, items: {}", event.orderId(), stockDeductionMap);
            
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
        }
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleDataPlatformSent(OrderEvent.DataPlatformSent event) {
        log.info("데이터 플랫폼 전송 이벤트 처리 시작 - orderId: {}, eventType: {}", 
                event.orderId(), event.eventType());
        
        try {
            
            log.info("데이터 플랫폼 전송 이벤트 처리 완료 - orderId: {}", event.orderId());
            
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 이벤트 처리 중 오류 발생 - orderId: {}, error: {}", 
                    event.orderId(), e.getMessage());
        }
    }
}
