package com.loopers.domain.order;

public abstract class OrderEvent {
    
    public record Completed(
        Long orderId,
        Long userId,
        Long totalAmount,
        Long discountAmount
    ) {
        public static Completed of(Long orderId, Long userId, Long totalAmount, Long discountAmount) {
            return new Completed(orderId, userId, totalAmount, discountAmount);
        }
    }
}
