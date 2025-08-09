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
        String userId = userService.findById(command.userId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .getUserId();

        if(command.couponId() != null) {
            discountAmount = couponService.calculateDiscount(command.couponId(), userId, totalAmount);
            couponService.useCoupon(command.couponId(), userId, totalAmount);
        }

        Long finalAmount = totalAmount - discountAmount;
        // 여기서 deduct자체가 검증이니까 valicate를 할필요없다
        productService.deductStock(command.getItemQuantityMap());
        pointService.deductPoint(userId, totalAmount);

        OrderEntity order = OrderEntity.from(commandWithPrice);
        order.applyDiscount(discountAmount);

        OrderEntity savedOrder = orderService.save(order);
        
        return OrderInfo.from(savedOrder);
    }

} 
