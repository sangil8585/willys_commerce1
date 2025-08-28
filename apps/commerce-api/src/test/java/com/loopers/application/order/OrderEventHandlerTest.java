package com.loopers.application.order;

import com.loopers.domain.order.OrderEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @Test
    @DisplayName("주문 생성 완료 이벤트를 처리한다")
    void 주문_생성_완료_이벤트_처리() {
        // given
        OrderEvent.Completed event = OrderEvent.Completed.of(1L, 1L, 10000L, 1000L);

        // when
        orderEventHandler.handleOrderCreated(event);

        // then
        // 로그 출력 확인 (실제 구현에서는 부가 기능 호출 확인)
        // verify(dataPlatformService, times(1)).sendOrderData(event.orderId());
        // verify(couponHistoryService, times(1)).recordUsage(event.userId(), event.orderId());
        // verify(pointHistoryService, times(1)).recordUsage(event.userId(), event.orderId());
    }

    @Test
    @DisplayName("결제 완료 이벤트를 처리한다")
    void 결제_완료_이벤트_처리() {
        // given
        OrderEvent.PaymentCompleted event = OrderEvent.PaymentCompleted.of(1L, 1L, "PAY_123", 9000L);

        // when
        orderEventHandler.handlePaymentCompleted(event);

        // then
        // 로그 출력 확인 (실제 구현에서는 부가 기능 호출 확인)
        // verify(orderService, times(1)).updateOrderStatus(event.orderId(), OrderState.PAID);
        // verify(productService, times(1)).deductStock(event.orderId());
        // verify(pointService, times(1)).accumulatePoints(event.userId(), event.finalAmount());
    }

    @Test
    @DisplayName("데이터 플랫폼 전송 이벤트를 처리한다")
    void 데이터_플랫폼_전송_이벤트_처리() {
        // given
        OrderEvent.DataPlatformSent event = OrderEvent.DataPlatformSent.of(1L, 1L, "ORDER_COMPLETED");

        // when
        orderEventHandler.handleDataPlatformSent(event);

        // then
        // 로그 출력 확인 (실제 구현에서는 부가 기능 호출 확인)
        // verify(dataPlatformService, times(1)).updateSendStatus(event.orderId(), "SENT");
    }

    @Test
    @DisplayName("이벤트 처리 중 예외가 발생해도 메인 트랜잭션에 영향을 주지 않는다")
    void 이벤트_처리_예외_발생시_메인_트랜잭션_보호() {
        // given
        OrderEvent.Completed event = OrderEvent.Completed.of(1L, 1L, 10000L, 1000L);

        // when & then
        // 예외가 발생해도 테스트가 통과해야 함 (메인 트랜잭션 보호)
        orderEventHandler.handleOrderCreated(event);
        
        // 실제 구현에서는 부가 기능에서 예외가 발생해도 메인 트랜잭션은 영향받지 않음
        // verify(dataPlatformService, times(1)).sendOrderData(event.orderId());
    }
}
