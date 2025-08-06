package com.loopers.application.product;

import com.loopers.domain.product.ProductEntity;

public record ProductInfo(
        Long id,
        String name,
        Long brandId,
        String brandName,
        Long price,
        Long stock,
        Long likes
) {
    public static ProductInfo from(ProductEntity productEntity, String brandName) {
        return new ProductInfo(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getBrandId(),
                brandName,
                productEntity.getPrice(),
                productEntity.getStock(),
                productEntity.getLikes()
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
                productEntity.getLikes()
        );
    }
}
