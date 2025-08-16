package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "product",
    indexes = {
        @Index(name = "idx_product_brand_id", columnList = "brand_id"),
        @Index(name = "idx_product_likes", columnList = "likes"),
        @Index(name = "idx_product_brand_likes", columnList = "brand_id,likes")
    }
)
public class ProductEntity extends BaseEntity {
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId;
    
    @Column(name = "price", nullable = false)
    private Long price;
    
    @Column(name = "stock", nullable = false)
    private Long stock;
    
    @Column(name = "likes", nullable = false)
    private Long likes;

    public static ProductEntity from(ProductCommand.Create command) {
        return new ProductEntity(
                command.name(),
                command.brandId(),
                command.price(),
                command.stock(),
                command.likes()
        );
    }

    protected ProductEntity() {}

    public ProductEntity(String name, Long brandId, Long price, Long stock, Long likes) {
        super();
        if(name == null || name.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 필수입니다.");
        }
        if(brandId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 필수입니다.");
        }
        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 0 이상이어야 합니다.");
        }
        if (stock == null || stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
        }
        this.name = name;
        this.brandId = brandId;
        this.price = price;
        this.stock = stock;
        this.likes = likes != null ? likes : 0L;
    }

    public void incrementLikes() {
        this.likes = (this.likes == null) ? 1L : this.likes + 1L;
    }

    public void decrementLikes() {
        if (this.likes != null && this.likes > 0) {
            this.likes = this.likes - 1L;
        }
    }

    public void deductStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "차감할 수량은 1개 이상이어야 합니다.");
        }
        
        if (this.stock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        
        this.stock = this.stock - quantity;
    }
}
