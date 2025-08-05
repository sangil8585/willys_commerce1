package com.loopers.domain.brand;

public class BrandCommand {
    public record Get(
            Long id
    ) {

    }

    public record Create(
            String name
    ) {
        public static Create of(String name) {
            return new Create(name);
        }
    }
}
