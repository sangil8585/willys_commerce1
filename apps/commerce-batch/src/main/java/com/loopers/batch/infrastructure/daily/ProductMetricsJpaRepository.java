package com.loopers.batch.infrastructure.daily;

import com.loopers.batch.domain.daily.ProductMetricsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetricsEntity, Long> {
    List<ProductMetricsEntity> findByDate(LocalDate date, Pageable pageable);
}


