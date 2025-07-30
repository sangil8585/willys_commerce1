package com.loopers.domain.product;

import java.util.List;

public record ProductCriteria(
        List<Criterion> criteria
) {
    public interface Criterion {}

    //정렬 조건
    public record OrderByCreatedAt(boolean ascending) implements Criterion {}
    public record OrderByPrice(boolean ascending) implements Criterion {}
    public record OrderByLikeCount() implements Criterion {}

    // 검색 조건
    public record NameContains(String name) implements Criterion {}
    public record BrandIdEquals(Long brandId) implements Criterion {}
    public record PriceRange(Long minPrice, Long maxPrice) implements Criterion {}
    public record StockGreaterThan(Long stock) implements Criterion {}
    public record LikesGreaterThan(Long likes) implements Criterion {}

    //정적 팩토리 메서드
    public static ProductCriteria orderByCreatedAt(boolean ascending) {
        return new ProductCriteria(List.of(new OrderByCreatedAt(ascending)));
    }

    public static ProductCriteria orderByPrice(boolean ascending) {
        return new ProductCriteria(List.of(new OrderByPrice(ascending)));
    }

    public static ProductCriteria nameContains(String name) {
        return new ProductCriteria(List.of(new NameContains(name)));
    }

    public static ProductCriteria brandIdEquals(Long brandId) {
        return new ProductCriteria(List.of(new BrandIdEquals(brandId)));
    }


    public static ProductCriteria stockGreaterThan(Long stock) {
        return new ProductCriteria(List.of(new StockGreaterThan(stock)));
    }
}
