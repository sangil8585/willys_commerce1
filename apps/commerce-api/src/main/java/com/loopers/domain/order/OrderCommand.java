package com.loopers.domain.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        public Map<Long, Integer> getItemQuantityMap() {
            return this.items.stream().collect(Collectors.toMap(
                    item -> item.productId, item -> item.quantity
            ));
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
