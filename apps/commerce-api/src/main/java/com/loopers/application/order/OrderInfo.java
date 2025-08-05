package com.loopers.application.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderItemEntity;

import java.util.List;

public record OrderInfo(
        Long id,
        Long userId,
        List<OrderItemInfo> items,
        Long totalAmount
) {
    public static OrderInfo from(OrderEntity orderEntity) {
        List<OrderItemInfo> itemInfos = orderEntity.getItems().stream()
                .map(OrderItemInfo::from)
                .toList();

        return new OrderInfo(
                orderEntity.getId(),
                orderEntity.getUserId(),
                itemInfos,
                orderEntity.getTotalAmount()
        );
    }

    public record OrderItemInfo(
            Long id,
            Long productId,
            Integer quantity,
            Long price
    ) {
        public static OrderItemInfo from(OrderItemEntity orderItemEntity) {
            return new OrderItemInfo(
                    orderItemEntity.getId(),
                    orderItemEntity.getProductId(),
                    orderItemEntity.getQuantity(),
                    orderItemEntity.getPrice()
            );
        }
    }
} 