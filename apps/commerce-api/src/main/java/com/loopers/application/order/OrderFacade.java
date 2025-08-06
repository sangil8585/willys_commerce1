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
        // 1. 상품 정보 조회 및 가격 설정
        List<OrderCommand.OrderItem> itemsWithPrice = command.items().stream()
                .map(item -> {
                    ProductEntity product = productService.findById(item.productId())
                            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
                    return OrderCommand.OrderItem.of(item.productId(), item.quantity(), product.getPrice());
                })
                .collect(Collectors.toList());
        
        OrderCommand.Create commandWithPrice = new OrderCommand.Create(command.userId(), itemsWithPrice, command.couponId());
        OrderEntity order = OrderEntity.from(commandWithPrice);

        // 2. 쿠폰 적용 및 할인 계산
        Long discountAmount = 0L;
        if (command.couponId() != null) {
            String userStringId = userService.findById(command.userId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                    .getUserId();
            
            discountAmount = couponService.calculateDiscount(command.couponId(), userStringId, order.getTotalAmount());
            order.applyDiscount(discountAmount);
        }

        // 3. 재고 검증 및 차감 (동시성 제어)
        validateAndDeductStock(command.items());

        // 4. 포인트 검증 및 차감 (동시성 제어)
        validateAndDeductPoints(order.getUserId(), order.getFinalAmount());

        // 5. 쿠폰 사용 처리 (동시성 제어)
        if (command.couponId() != null) {
            String userStringId = userService.findById(command.userId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                    .getUserId();
            
            couponService.useCoupon(command.couponId(), userStringId, order.getTotalAmount());
        }

        // 6. 주문 저장
        OrderEntity savedOrder = orderService.save(order);
        
        return OrderInfo.from(savedOrder);
    }

    private void validateAndDeductStock(List<OrderCommand.OrderItem> items) {
        for (OrderCommand.OrderItem item : items) {
            productService.validateAndDeductStock(item.productId(), item.quantity());
        }
    }

    // 비관락으로 포인트처리리
    private void validateAndDeductPoints(Long userId, Long totalAmount) {
        String userStringId = userService.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .getUserId();
        
        // 비관적 락을 사용한 포인트 차감
        pointService.deductPoint(userStringId, totalAmount);
    }
} 
