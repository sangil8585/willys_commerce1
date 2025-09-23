package com.loopers.batch.infrastructure.rollup;

import com.loopers.batch.domain.rollup.ProductRankWeeklyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ProductRankWeeklyJpaRepository extends JpaRepository<ProductRankWeeklyEntity, Long> {
    void deleteByAsOfDate(LocalDate asOfDate);
}


