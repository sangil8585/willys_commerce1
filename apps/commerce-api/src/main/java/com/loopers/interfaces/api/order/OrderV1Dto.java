package com.loopers.interfaces.api.order;

import java.util.List;


import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;

public class OrderV1Dto {
    public static class Request{
        public record Order(
            Long userId,
            String paymentType,
            List<Item> orderItems,
            List<Long> orderCouponIds
        ) {
            public OrderCriteria.Order toCriteria() {
                List<OrderCriteria.Item> items = orderItems.stream()
                    .map(item -> new OrderCriteria.Item(item.productId(), item.quantity()))
                    .toList();
                return new OrderCriteria.Order(userId, paymentType, items, orderCouponIds);
            }
        }

        public record Item(
            Long productId,
            Long quantity
        ) {}
    }

    public static class Response {
        public record Order(
            Long orderId,
            Long userId,
            String orderDate,
            long totalPrice,
            String state
        ) {

            public static Order from(OrderResult.OrderResponse response) {
                return new Order(
                    response.orderId(),
                    response.userId(),
                    response.orderDate(),
                    response.totalPrice(),
                    response.state()
                );
            }
        }

        public record Detail(
            Long orderId,
            Long userId,
            String orderDate,
            long totalPrice,
            String state,
            List<Item> items,
            List<Coupon> couponIds
        ) {

        }

        public record Item(
                Long productId,
                Long quantity,
                Long price
        ) {
            public Item(OrderResult.Item item) {
                this(item.productId(), item.quantity(), item.price());
            }
        }

        public record Coupon(
                Long id,
                Long value
        ) {
            public Coupon(OrderResult.Coupon coupon) {
                this(coupon.couponId(), coupon.value());
            }
        }
    }

}
