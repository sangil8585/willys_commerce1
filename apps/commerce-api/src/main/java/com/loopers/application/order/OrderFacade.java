package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final PointService pointService;

    @Transactional
    public OrderInfo createOrder(OrderCommand.Create command) {
        List<OrderCommand.OrderItem> itemsWithPrice = command.items().stream()
                .map(item -> {
                    ProductEntity product = productService.findById(item.productId())
                            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
                    return OrderCommand.OrderItem.of(item.productId(), item.quantity(), product.getPrice());
                })
                .collect(Collectors.toList());
        
        OrderCommand.Create commandWithPrice = new OrderCommand.Create(command.userId(), itemsWithPrice);

        OrderEntity order = OrderEntity.from(commandWithPrice);

        validateAndDeductStock(command.items());

        validateAndDeductPoints(order.getUserId(), order.getTotalAmount());

        OrderEntity savedOrder = orderService.save(order);
        
        return OrderInfo.from(savedOrder);
    }

    private void validateAndDeductStock(java.util.List<OrderCommand.OrderItem> items) {
        for (OrderCommand.OrderItem item : items) {
            productService.validateAndDeductStock(item.productId(), item.quantity());
        }
    }

    private void validateAndDeductPoints(Long userId, Long totalAmount) {
        // 사용자 정보 조회하여 올바른 userId(String) 가져오기
        String userStringId = userService.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .getUserId();
        
        // 현재 포인트 조회
        Long currentPoints = pointService.get(userStringId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 포인트 정보를 찾을 수 없습니다."));
        
        if (currentPoints < totalAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        
        // 포인트 차감
        pointService.charge(userStringId, -totalAmount);
    }
} 
