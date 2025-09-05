package com.loopers.event.order;

import com.loopers.event.BaseEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderKafkaEvent extends BaseEvent {
    
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String orderState;
    private List<OrderItem> items;
    
    public OrderKafkaEvent(String eventType, Long orderId, Long userId, BigDecimal totalAmount, 
                     BigDecimal discountAmount, String orderState, List<OrderItem> items) {
        super(eventType, "commerce-api", "1.0");
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.orderState = orderState;
        this.items = items;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItem {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        
        public OrderItem(Long productId, Integer quantity, BigDecimal price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
    }
    
    // 주문 완료 이벤트
    public static OrderKafkaEvent orderCompleted(Long orderId, Long userId, BigDecimal totalAmount, 
                                          BigDecimal discountAmount, List<OrderItem> items) {
        return new OrderKafkaEvent("OrderCompleted", orderId, userId, totalAmount, discountAmount, "COMPLETED", items);
    }
    
    // 결제 완료 이벤트
    public static OrderKafkaEvent paymentCompleted(Long orderId, Long userId, BigDecimal finalAmount, 
                                            List<OrderItem> items) {
        return new OrderKafkaEvent("PaymentCompleted", orderId, userId, finalAmount, BigDecimal.ZERO, "PAID", items);
    }
}
