package com.loopers.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    OrderEntity save(OrderEntity orderEntity);

    Optional<OrderEntity> findById(Long orderId);

    List<OrderEntity> findByUserId(Long userId);
} 
