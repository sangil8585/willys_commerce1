package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    private final CouponService couponService;
    
    @Transactional
    public OrderResult.OrderResponse createOrder(OrderCriteria.Order criteria) {
        // 유저 확인
        userService.findByUserId(criteria.userId()).orElseThrow(() -> 
            new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다: " + criteria.userId())
        );
        
        // 재고 확보(락 걸기)
        List<ProductEntity> targetProducts = productService.findByIds(criteria.getProductIds());
        if (targetProducts.size() != criteria.getProductIds().size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "일부 상품을 찾을 수 없습니다");
        }
        
        // 상품 가격 매핑 생성
        Map<Long, Long> productPriceMap = targetProducts.stream()
                .collect(Collectors.toMap(
                        ProductEntity::getId,
                        ProductEntity::getPrice
                ));
        
        // 주문 아이템 생성 (가격 정보 포함)
        OrderCommand.Create orderCommand = new OrderCommand.Create(
                criteria.userId(),
                criteria.items().stream()
                        .map(item -> new OrderCommand.OrderItem(
                                item.productId(), 
                                item.quantity(),
                                productPriceMap.get(item.productId())
                        ))
                        .toList(),
                criteria.couponIds().isEmpty() ? null : criteria.couponIds().get(0)
        );
        
        Long totalAmount = criteria.items().stream()
                .mapToLong(item -> productPriceMap.get(item.productId()) * item.quantity())
                .sum();
        log.debug("최초 주문 총액 계산(쿠폰미적용): {}", totalAmount);
        
        
        // 쿠폰 사용 여부 확인 및 적용
        Long discountAmount = 0L;
        if(criteria.couponIds() != null && !criteria.couponIds().isEmpty()) {
            Long couponId = criteria.couponIds().get(0);
            discountAmount = couponService.calculateDiscount(couponId, criteria.userId().toString(), totalAmount);
            couponService.useCoupon(couponId, criteria.userId(), totalAmount);
            Long finalAmount = totalAmount - discountAmount;
            
            log.debug("쿠폰 적용 - 쿠폰ID: {}, 할인 금액: {}, 최종 금액: {}", 
                    couponId, discountAmount, finalAmount);
        }
        
        
        // 재고 차감 (비관적 락)
        Map<Long, Integer> stockDeductionMap = criteria.items().stream()
                .collect(Collectors.toMap(
                        OrderCriteria.Item::productId,
                        item -> item.quantity().intValue()
                ));
        productService.deductStock(stockDeductionMap);
        
        // 주문 생성 (도메인 로직)
        OrderEntity createdOrder = orderService.createOrder(orderCommand, productPriceMap);
        
        // 결과 반환 (실제 생성된 주문 데이터)
        OrderResult.OrderResponse result = OrderResult.OrderResponse.from(createdOrder);
        
        log.info("주문 생성 완료 - orderId: {}, 총액: {}, 할인: {}, 최종금액: {}", 
                result.orderId(), totalAmount, discountAmount, totalAmount - discountAmount);
        
        return result;
    }
} 
