package com.loopers.application.order;

import java.util.List;

import com.loopers.domain.order.OrderEntity;

public class OrderResult {
    public record OrderResponse(
        Long orderId,
        Long userId,
        String orderDate,
        long totalPrice,
        String state,
        String paymentId
    ) {
        public static OrderResponse from(OrderEntity order) {
            return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalAmount(),
                order.getStateDescription(),
                null
            );
        }
        
        public static OrderResponse from(OrderEntity order, String paymentId) {
            return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalAmount(),
                order.getStateDescription(),
                paymentId
            );
        }
    }

    public record Item(
        Long productId,
        Long quantity,
        Long price
    ) {
        public static Item from(com.loopers.domain.order.OrderItemEntity orderItem) {
            return new Item(
                orderItem.getProductId(),
                orderItem.getQuantity(),
                orderItem.getPrice()
            );
        }
    }

    public record Coupon(
        Long couponId,
        Long value
    ) {
        public static Coupon of(Long couponId, Long value) {
            return new Coupon(couponId, value);
        }
    }
}
