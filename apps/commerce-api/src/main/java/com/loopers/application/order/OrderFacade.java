package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
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
    private final CouponService couponService;

    @Transactional
    public OrderInfo createOrder(OrderCommand.Create command) {
        List<OrderCommand.OrderItem> itemsWithPrice = command.items().stream()
                .map(item -> {
                    ProductEntity product = productService.findById(item.productId())
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
                    return OrderCommand.OrderItem.of(item.productId(), item.quantity(), product.getPrice());
                })
                .collect(Collectors.toList());
        
        OrderCommand.Create commandWithPrice = new OrderCommand.Create(command.userId(), itemsWithPrice, command.couponId());
        
        Long totalAmount = itemsWithPrice.stream()
                .mapToLong(item -> item.price() * item.quantity())
                .sum();

        Long discountAmount = 0L;
        // 쿠폰 사용을 먼저 처리 (비관적 락으로 동시성 제어)
        if (command.couponId() != null) {
            String userStringId = userService.findById(command.userId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                    .getUserId();
            
            discountAmount = couponService.calculateDiscount(command.couponId(), userStringId, totalAmount);
            couponService.useCoupon(command.couponId(), userStringId, totalAmount);
        }

        Long finalAmount = totalAmount - discountAmount;
        validateAndDeductStock(command.items());
        validateAndDeductPoints(command.userId(), finalAmount);

        OrderEntity order = OrderEntity.from(commandWithPrice);
        order.applyDiscount(discountAmount);

        OrderEntity savedOrder = orderService.save(order);
        
        return OrderInfo.from(savedOrder);
    }

    private void validateAndDeductStock(List<OrderCommand.OrderItem> items) {
        for (OrderCommand.OrderItem item : items) {
            productService.validateAndDeductStock(item.productId(), item.quantity());
        }
    }

    private void validateAndDeductPoints(Long userId, Long totalAmount) {
        String userStringId = userService.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .getUserId();
        
        pointService.deductPoint(userStringId, totalAmount);
    }
} 
