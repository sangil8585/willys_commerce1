package com.loopers.domain.metrics;

import com.loopers.domain.metrics.ProductMetricsEntity;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricsRepository {
    
    ProductMetricsEntity save(ProductMetricsEntity metrics);
    
    Optional<ProductMetricsEntity> findByProductIdAndDate(Long productId, LocalDate date);
    
    boolean existsByProductIdAndDate(Long productId, LocalDate date);
}
