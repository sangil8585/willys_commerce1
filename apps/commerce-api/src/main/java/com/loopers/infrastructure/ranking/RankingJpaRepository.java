package com.loopers.infrastructure.ranking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RankingJpaRepository extends JpaRepository<RankingEntity, Long> {

    /**
     * 특정 날짜의 랭킹된 상품 ID들을 조회합니다.
     * 주문량 기준으로 정렬하여 상위 상품들을 반환합니다.
     */
    @Query("""
        SELECT r.productId 
        FROM RankingEntity r 
        WHERE r.date = :date 
        ORDER BY r.orderCount DESC, r.productId ASC
        """)
    List<Long> findRankedProducts(@Param("date") LocalDate date, Pageable pageable);

    /**
     * 특정 날짜의 랭킹 데이터 총 개수를 조회합니다.
     */
    @Query("SELECT COUNT(r) FROM RankingEntity r WHERE r.date = :date")
    Long countByDate(@Param("date") LocalDate date);
}
