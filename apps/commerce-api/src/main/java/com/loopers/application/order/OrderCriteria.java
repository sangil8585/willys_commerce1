package com.loopers.application.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class OrderCriteria {
        public record Item(
                Long productId,
                Long quantity
        ) {
        }

        public record Order(
                Long userId,
                String paymentType,
                String cardType,
                String cardNo,
                String callbackUrl,
                List<Item> items,
                List<Long> couponIds
        ) {
                public Map<Long, Long> getItemQuantityMap() {
                        return items.stream()
                                .collect(Collectors.toMap(
                                        OrderCriteria.Item::productId,
                                        OrderCriteria.Item::quantity
                                ));
                }
                
                public List<Long> getProductIds() {
                        return items.stream()
                                .map(Item::productId)
                                .toList();
                }
                
                public Long getTotalQuantity() {
                        return items.stream()
                                .mapToLong(Item::quantity)
                                .sum();
                }
        }
        
}
