package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.application.order.OrderCriteria;
import com.loopers.domain.order.OrderEntity;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
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
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public OrderResult.OrderResponse createOrder(OrderCriteria.Order criteria) {
        // 유저 확인
        userService.findByUserId(criteria.userId()).orElseThrow(() -> 
            new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다: " + criteria.userId())
        );
        
        // 재고 확인 (락 걸기)
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
        
        // 주문 아이템 생성
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
            discountAmount = couponService.calculateDiscount(couponId, criteria.userId(), totalAmount);
            couponService.useCoupon(couponId, criteria.userId(), totalAmount);
            Long finalAmount = totalAmount - discountAmount;
            
            log.debug("쿠폰 적용 - 쿠폰ID: {}, 할인 금액: {}, 최종 금액: {}", 
                    couponId, discountAmount, finalAmount);
        }
        
        // 주문 생성 (CREATED 상태로 생성, 재고 차감 없음)
        OrderEntity createdOrder = orderService.createOrder(orderCommand, productPriceMap);
        
        // 결제 정보 생성 (PENDING 상태)
        PaymentCommand.Create paymentCommand = PaymentCommand.Create.of(
                criteria.userId(),
                createdOrder.getId().toString(),
                criteria.cardType(),
                criteria.cardNo(),
                String.valueOf(totalAmount - discountAmount),
                criteria.callbackUrl()
        );
        
        PaymentEntity payment = paymentService.createPayment(paymentCommand);
        
        log.info("주문 생성 완료 - orderId: {}, 총액: {}, 할인: {}, 최종금액: {}, 결제ID: {}", 
                createdOrder.getId(), totalAmount, discountAmount, totalAmount - discountAmount, payment.getPaymentId());
        
        // 주문 생성 완료 이벤트 발행
        OrderEvent.Completed orderCompletedEvent = OrderEvent.Completed.of(
                createdOrder.getId(),
                createdOrder.getUserId(),
                createdOrder.getTotalAmount(),
                createdOrder.getDiscountAmount()
        );
        eventPublisher.publishEvent(orderCompletedEvent);
        
        // 결과 반환
        return OrderResult.OrderResponse.from(createdOrder, payment.getPaymentId());
    }
    
    /**
     * 결제 완료 후 주문 완료 처리
     * Payment 도메인에서 호출하여 주문 상태를 완료로 변경하고 재고를 차감합니다.
     */
    @Transactional
    public void completeOrderAfterPayment(Long orderId) {
        OrderEntity order = orderService.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));
        
        if (!"CREATED".equals(order.getState())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "처리할 수 없는 주문 상태입니다: " + order.getState());
        }
        
        try {
            // 주문 완료 처리
            order.complete();
            orderService.save(order);
            
            // 재고 차감 (결제 완료 후)
            Map<Long, Integer> stockDeductionMap = order.getItems().stream()
                    .collect(Collectors.toMap(
                            item -> item.getProductId(),
                            item -> item.getQuantity().intValue()
                    ));
            productService.deductStock(stockDeductionMap);
            
            
            PaymentEntity payment = paymentService.findByOrderId(orderId.toString());
            
            // 결제 완료 이벤트 발행
            OrderEvent.PaymentCompleted paymentCompletedEvent = OrderEvent.PaymentCompleted.of(
                    order.getId(),
                    order.getUserId(),
                    payment.getPaymentId(),
                    order.getFinalAmount()
            );
            eventPublisher.publishEvent(paymentCompletedEvent);
            
            // 데이터 플랫폼 전송 이벤트 발행
            OrderEvent.DataPlatformSent dataPlatformEvent = OrderEvent.DataPlatformSent.of(
                    order.getId(),
                    order.getUserId(),
                    "ORDER_COMPLETED"
            );
            eventPublisher.publishEvent(dataPlatformEvent);
            
            log.info("결제 완료 후 주문 완료 처리 완료 - orderId: {}, 재고 차감 완료", orderId);
            
        } catch (Exception e) {
            // 주문 실패 처리
            order.markAsFailed();
            orderService.save(order);
            
            log.error("결제 완료 후 주문 처리 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "주문 처리에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 결제 실패 시 주문 취소 처리
     * Payment 도메인에서 호출하여 주문 상태를 취소로 변경합니다.
     */
    @Transactional
    public void cancelOrderAfterPaymentFailure(Long orderId, String reason) {
        OrderEntity order = orderService.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));
        
        if (!"CREATED".equals(order.getState())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "취소할 수 없는 주문 상태입니다: " + order.getState());
        }
        
        // 주문 취소 처리
        order.cancel();
        orderService.save(order);
        
        log.info("결제 실패로 인한 주문 취소 완료 - orderId: {}, 사유: {}", orderId, reason);
    }
} 
