package com.loopers.application.order;

import com.loopers.config.kafka.KafkaEventPublisher;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.coupon.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerPartitionTest {

    @Mock
    private CouponService couponService;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private OrderEventHandler orderEventHandler;

    @BeforeEach
    void setUp() {
        orderEventHandler = new OrderEventHandler(
            couponService, productService, orderService, kafkaEventPublisher
        );
    }

    @Test
    void 주문완료_이벤트_발행시_orderId가_파티션키로_사용된다() {
        // given
        Long orderId = 12345L;
        Long userId = 1L;
        Long totalAmount = 10000L;
        Long discountAmount = 1000L;
        
        OrderEvent.Completed event = OrderEvent.Completed.of(
            orderId, userId, totalAmount, discountAmount
        );

        // when
        orderEventHandler.handleOrderCreated(event);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<com.loopers.event.order.OrderKafkaEvent> eventCaptor = ArgumentCaptor.forClass(com.loopers.event.order.OrderKafkaEvent.class);

        verify(kafkaEventPublisher).publishEventAsync(
            topicCaptor.capture(), 
            keyCaptor.capture(), 
            eventCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("order-events");
        assertThat(keyCaptor.getValue()).isEqualTo(orderId.toString());
        assertThat(eventCaptor.getValue()).isNotNull();
    }

    @Test
    void 결제완료_이벤트_발행시_orderId가_파티션키로_사용된다() {
        // given
        Long orderId = 12345L;
        Long userId = 1L;
        String paymentId = "PAY_12345";
        Long finalAmount = 9000L;
        
        OrderEvent.PaymentCompleted event = OrderEvent.PaymentCompleted.of(
            orderId, userId, paymentId, finalAmount
        );

        // when
        orderEventHandler.handlePaymentCompleted(event);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<com.loopers.event.order.OrderKafkaEvent> eventCaptor = ArgumentCaptor.forClass(com.loopers.event.order.OrderKafkaEvent.class);

        verify(kafkaEventPublisher).publishEventAsync(
            topicCaptor.capture(), 
            keyCaptor.capture(), 
            eventCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("order-events");
        assertThat(keyCaptor.getValue()).isEqualTo(orderId.toString());
        assertThat(eventCaptor.getValue()).isNotNull();
    }

    @Test
    void 동일한_orderId를_가진_여러_이벤트가_같은_파티션키를_사용한다() {
        // given
        Long orderId = 12345L;
        Long userId = 1L;
        
        OrderEvent.Completed completedEvent = OrderEvent.Completed.of(
            orderId, userId, 10000L, 1000L
        );
        
        OrderEvent.PaymentCompleted paymentEvent = OrderEvent.PaymentCompleted.of(
            orderId, userId, "PAY_12345", 9000L
        );

        // when
        orderEventHandler.handleOrderCreated(completedEvent);
        orderEventHandler.handlePaymentCompleted(paymentEvent);

        // then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaEventPublisher, times(2)).publishEventAsync(
            any(), keyCaptor.capture(), any()
        );

        List<String> capturedKeys = keyCaptor.getAllValues();
        assertThat(capturedKeys).hasSize(2);
        assertThat(capturedKeys.get(0)).isEqualTo(orderId.toString());
        assertThat(capturedKeys.get(1)).isEqualTo(orderId.toString());
    }
}
