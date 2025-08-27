package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity {
    
    private Long userId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItemEntity> items = new ArrayList<>();
    
    private Long totalAmount;
    private Long discountAmount = 0L;
    
    @Enumerated(EnumType.STRING)
    private OrderState state = OrderState.PENDING;

    protected OrderEntity() {}

    public static OrderEntity from(OrderCommand.Create command) {
        
        OrderEntity order = new OrderEntity();
        order.userId = command.userId();
        
        for (OrderCommand.OrderItem itemCommand : command.items()) {
            if (itemCommand.productId() == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
            if (itemCommand.quantity() == null || itemCommand.quantity() <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
            }
            
            OrderItemEntity item = new OrderItemEntity(order, itemCommand.productId(), itemCommand.quantity(), itemCommand.price());
            order.items.add(item);
        }
        
        order.calculateTotalAmount();
        return order;
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    public void applyDiscount(Long discountAmount) {
        if (discountAmount == null || discountAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 0 이상이어야 합니다.");
        }
        if (discountAmount > this.totalAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 주문 총액을 초과할 수 없습니다.");
        }
        this.discountAmount = discountAmount;
    }
    
    public Long getFinalAmount() {
        return this.totalAmount - this.discountAmount;
    }
    
    public String getState() {
        return this.state.name();
    }
    
    public String getStateDescription() {
        return this.state.getDescription();
    }
    
    public void complete() {
        if (state != OrderState.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태가 PENDING이 아닙니다. 현재 상태: " + state.getDescription());
        }
        this.state = OrderState.COMPLETED;
    }

    public void cancel() {
        log.info("OrderEntity.cancel 시작 - orderId: {}, 현재 상태: {}", this.getId(), this.state);
        this.state = OrderState.CANCELLED;
        log.info("OrderEntity.cancel 완료 - orderId: {}, 변경된 상태: {}", this.getId(), this.state);
    }
    
    public void markAsCreated() {
        this.state = OrderState.CREATED;
    }
    
    public void markAsFailed() {
        this.state = OrderState.FAILED;
    }
    
    public boolean isCancellable() {
        return state == OrderState.PENDING || state == OrderState.CREATED;
    }
    
    public boolean isCompleted() {
        return state == OrderState.COMPLETED;
    }

    public void addItem(OrderCommand.OrderItem itemCommand) {
        if (itemCommand.productId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
        if (itemCommand.quantity() == null || itemCommand.quantity() <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
        }
        
        OrderItemEntity item = new OrderItemEntity(this, itemCommand.productId(), itemCommand.quantity(), itemCommand.price());
        this.items.add(item);
        calculateTotalAmount();
    }
}
