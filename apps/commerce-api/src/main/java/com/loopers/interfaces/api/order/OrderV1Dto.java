package com.loopers.interfaces.api.order;

import com.loopers.domain.order.OrderCommand;

import java.util.List;

public class OrderV1Dto {
    public record V1() {
        public record OrderResponse(
            Long id
            Long user

        ) {
            public OrderCommand toCriteria() {
                List
            }
        }
    }
}
