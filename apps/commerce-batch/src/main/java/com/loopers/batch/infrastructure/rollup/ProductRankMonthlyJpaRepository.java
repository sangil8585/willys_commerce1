package com.loopers.batch.infrastructure.rollup;

import com.loopers.batch.domain.rollup.ProductRankMonthlyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ProductRankMonthlyJpaRepository extends JpaRepository<ProductRankMonthlyEntity, Long> {
    void deleteByAsOfDate(LocalDate asOfDate);
}


