package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity {
    
    private Long userId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItemEntity> items = new ArrayList<>();
    
    private Long totalAmount;

    protected OrderEntity() {}

    public static OrderEntity from(OrderCommand.Create command) {
        if (command.userId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        
        if (command.items() == null || command.items().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 아이템은 필수입니다.");
        }
        
        OrderEntity order = new OrderEntity();
        order.userId = command.userId();
        
        for (OrderCommand.OrderItem itemCommand : command.items()) {
            if (itemCommand.productId() == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
            }
            if (itemCommand.quantity() == null || itemCommand.quantity() <= 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
            }
            
            // 실제 상품 가격을 가져와서 OrderItemEntity 생성
            OrderItemEntity item = new OrderItemEntity(itemCommand.productId(), itemCommand.quantity(), itemCommand.price());
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

    public void addItem(OrderCommand.OrderItem itemCommand) {
        if (itemCommand.productId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 ID는 필수입니다.");
        }
        if (itemCommand.quantity() == null || itemCommand.quantity() <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1개 이상이어야 합니다.");
        }
        
        OrderItemEntity item = new OrderItemEntity(itemCommand.productId(), itemCommand.quantity(), itemCommand.price());
        this.items.add(item);
        calculateTotalAmount();
    }
}
