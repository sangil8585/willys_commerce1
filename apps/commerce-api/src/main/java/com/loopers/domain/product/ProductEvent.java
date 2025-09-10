package com.loopers.domain.product;

public abstract class ProductEvent {
    
    public record Viewed(
        Long productId
    ) {
        public static Viewed of(Long productId) {
            return new Viewed(productId);
        }
    }
}
