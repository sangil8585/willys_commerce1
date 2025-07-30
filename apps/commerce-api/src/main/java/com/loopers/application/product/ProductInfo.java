package com.loopers.application.product;

import com.loopers.domain.product.ProductEntity;

public record ProductInfo(
        Long id,
        String name,
        Long brandId,
        Long price,
        Long stock,
        Long likes
) {
    public static ProductInfo from(ProductEntity productEntity) {
        return new ProductInfo(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getBrandId(),
                productEntity.getPrice(),
                productEntity.getStock(),
                productEntity.getLikes()
        );
    }
}
