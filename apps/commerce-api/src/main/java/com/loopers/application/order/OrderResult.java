package com.loopers.application.order;

import java.util.List;

import com.loopers.domain.order.OrderEntity;

public class OrderResult {
    public record OrderResponse(
        Long orderId,
        Long userId,
        String orderDate,
        long totalPrice,
        String state
    ) {
        public static OrderResponse from(OrderEntity order) {
            return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt().toString(),
                order.getTotalAmount(),
                order.getStateDescription()
            );
        }
    }
}
