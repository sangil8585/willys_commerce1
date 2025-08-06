package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;

import java.util.List;

public class OrderCriteria {
    public record Item(
            Long productId,
            Long quantity
    ) {
    }

    public record Order(
            Long userId,
            List<Item> orderItems
    ) {
        public OrderCommand.Create toCommand() {

            return new OrderCommand.Create(userId, items);
        }

        public Map<Long, Long> getOrderItemMap() {
            return orderItems.stream()
                    .collect(Collectors.toMap(
                            OrderCriteria.Item::productId,
                            OrderCriteria.Item::quantity
                    ));
        }
    }
}
