package com.loopers.domain.ranking;


import java.time.LocalDate;
import java.util.List;

public interface RankingRepository {
    List<Long> getRankedProducts(int offset, int size, LocalDate date, RankingPeriod period);

    Long getTotalCount(LocalDate date, RankingPeriod period);

    Long getProductRank(Long productId, LocalDate date, RankingPeriod period);
}
