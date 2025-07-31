package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "product")
public class ProductEntity extends BaseEntity {
    private String name;
    private Long brandId;
    private Long price;
    private Long stock;
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
        if(name == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if(brandId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (stock == null || stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.name = name;
        this.brandId = brandId;
        this.price = price;
        this.stock = stock;
        this.likes = 0L;
    }

    public void incrementLikes() {
        this.likes = (this.likes == null) ? 1L : this.likes + 1L;
    }

    public void decrementLikes() {
        if (this.likes != null && this.likes > 0) {
            this.likes = this.likes - 1L;
        }
    }       
}
