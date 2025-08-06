package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {
    public record Create(
            Long userId,
            List<OrderItem> items,
            Long couponId
    ) {
        public static Create of(Long userId, List<OrderItem> items) {
            return new Create(userId, items, null);
        }
        
        public static Create of(Long userId, List<OrderItem> items, Long couponId) {
            return new Create(userId, items, couponId);
        }
    }

    public record OrderItem(
            Long productId,
            Integer quantity,
            Long price
    ) {
        public static OrderItem of(Long productId, Integer quantity, Long price) {
            return new OrderItem(productId, quantity, price);
        }
    }
} 