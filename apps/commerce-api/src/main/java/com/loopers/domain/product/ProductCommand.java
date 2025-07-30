package com.loopers.domain.product;

public class ProductCommand {
    public record Create(
            String name,
            Long brandId,
            Long price,
            Long stock,
            Long likes
    ) {
        public static Create of(
                String name,
                Long brandId,
                Long price,
                Long stock,
                Long likes
        ) {
            return new Create(name, brandId, price, stock, likes);
        }
    }

}
