package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItemEntity extends BaseEntity {
    
    private Long productId;
    private Integer quantity;
    private Long price;

    public OrderItemEntity(Long productId, Integer quantity, Long price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    protected OrderItemEntity() {}
} 