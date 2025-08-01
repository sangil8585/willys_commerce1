package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public OrderEntity save(OrderEntity orderEntity) {
        return orderJpaRepository.save(orderEntity);
    }

    @Override
    public Optional<OrderEntity> findById(Long orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public List<OrderEntity> findByUserId(Long userId) {
        return orderJpaRepository.findByUserId(userId);
    }
} 
