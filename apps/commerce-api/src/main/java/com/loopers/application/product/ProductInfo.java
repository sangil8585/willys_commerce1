package com.loopers.application.product;

import com.loopers.domain.product.ProductEntity;
import java.io.Serializable;

public record ProductInfo(
        Long id,
        String name,
        Long brandId,
        String brandName,
        Long price,
        Long stock,
        Long likes,
        Long rank
) implements Serializable {
    public static ProductInfo from(ProductEntity productEntity, String brandName) {
        return new ProductInfo(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getBrandId(),
                brandName,
                productEntity.getPrice(),
                productEntity.getStock(),
                productEntity.getLikes(),
                null
        );
    }

    public static ProductInfo from(ProductEntity productEntity) {
        return new ProductInfo(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getBrandId(),
                null,
                productEntity.getPrice(),
                productEntity.getStock(),
                productEntity.getLikes(),
                null
        );
    }

    public static ProductInfo from(ProductEntity productEntity, String brandName, Long rank) {
        return new ProductInfo(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getBrandId(),
                brandName,
                productEntity.getPrice(),
                productEntity.getStock(),
                productEntity.getLikes(),
                rank
        );
    }
}
