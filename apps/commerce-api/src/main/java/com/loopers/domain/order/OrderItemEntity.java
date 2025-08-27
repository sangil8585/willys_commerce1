package com.loopers.domain.order;

import java.util.List;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItemEntity extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private Long productId;
    private Long quantity;
    private Long price;

    public OrderItemEntity(OrderEntity order, Long productId, Long quantity, Long price) {
        super();
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public static List<OrderItemEntity> from(OrderEntity order, List<OrderCommand.OrderItem> orderItemsCommand) {
        if (orderItemsCommand == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 아이템 정보가 없습니다.");
        }
        
        return orderItemsCommand.stream()
                .map(item -> new OrderItemEntity(order, item.productId(), item.quantity(), item.price()))
                .toList();
    }
} 