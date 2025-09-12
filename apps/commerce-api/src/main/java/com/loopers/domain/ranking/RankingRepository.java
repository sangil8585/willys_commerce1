package com.loopers.domain.ranking;


import java.time.LocalDate;
import java.util.List;

public interface RankingRepository {
    List<Long> getRankedProducts(int offset, int size, LocalDate date);

    Long getTotalCount(LocalDate date);

    Long getProductRank(Long productId, LocalDate date);
}
