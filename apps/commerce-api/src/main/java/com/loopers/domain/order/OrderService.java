package com.loopers.domain.order;

import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Optional<OrderEntity> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }
    
    @Transactional
    public OrderEntity createOrder(OrderCommand.Create command, Map<Long, Long> productPriceMap) {
        List<OrderCommand.OrderItem> itemsWithPrice = command.items().stream()
                .map(item -> new OrderCommand.OrderItem(
                        item.productId(),
                        item.quantity(),
                        productPriceMap.get(item.productId())
                ))
                .toList();
        
        OrderCommand.Create commandWithPrice = new OrderCommand.Create(
                command.userId(),
                itemsWithPrice,
                command.couponId()
        );
        
        OrderEntity order = OrderEntity.from(commandWithPrice);
        order.markAsCreated();
        
        return orderRepository.save(order);
    }
}
