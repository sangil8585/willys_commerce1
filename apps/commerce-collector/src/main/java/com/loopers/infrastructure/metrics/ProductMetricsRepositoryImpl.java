package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetricsEntity;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository jpaRepository;

    @Override
    public ProductMetricsEntity save(ProductMetricsEntity metrics) {
        return jpaRepository.save(metrics);
    }

    @Override
    public Optional<ProductMetricsEntity> findByProductIdAndDate(Long productId, LocalDate date) {
        return jpaRepository.findByProductIdAndDate(productId, date);
    }

    @Override
    public boolean existsByProductIdAndDate(Long productId, LocalDate date) {
        return jpaRepository.existsByProductIdAndDate(productId, date);
    }

    public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetricsEntity, Long> {
        Optional<ProductMetricsEntity> findByProductIdAndDate(Long productId, LocalDate date);
        boolean existsByProductIdAndDate(Long productId, LocalDate date);
    }
}
