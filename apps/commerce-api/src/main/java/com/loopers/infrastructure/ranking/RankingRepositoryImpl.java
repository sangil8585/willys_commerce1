package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingRedisTemplate rankingRedisTemplate;

    @Override
    public List<Long> getRankedProducts(int offset, int size, LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);
        Set<String> productIds = rankingRedisTemplate.getReverseRange(rankingKey, offset, offset + size - 1);

        return productIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public Long getTotalCount(LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);
        Long count = rankingRedisTemplate.getSize(rankingKey);

        return count != null ? count : 0L;
    }

    @Override
    public Long getProductRank(Long productId, LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);
        String member = productId.toString();

        Long rank = rankingRedisTemplate.getReverseRank(rankingKey, member);

        return rank + 1;
    }
}