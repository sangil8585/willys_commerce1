package com.loopers.domain.ranking;

import com.loopers.infrastructure.ranking.RankingRedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private final RankingRedisTemplate rankingRedisTemplate;

    private static final long DEFAULT_TTL_SECONDS = 2 * 24 * 60 * 60L;

    public void updateScore(Long productId, double score, LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);
        String member = productId.toString();

        rankingRedisTemplate.incrementScore(rankingKey, member, score);

        if (rankingRedisTemplate.getSize(rankingKey) == 1) {
            rankingRedisTemplate.setTTL(rankingKey, DEFAULT_TTL_SECONDS);
        }
    }

    public List<Long> getRankedProducts(int offset, int size, LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);

        Set<String> productIds = rankingRedisTemplate.getReverseRange(rankingKey, offset, offset + size - 1);

        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        return productIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public Long getTotalCount(LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);
        Long count = rankingRedisTemplate.getSize(rankingKey);
        return count != null ? count : 0L;
    }

    public Long getProductRank(Long productId, LocalDate date) {
        String rankingKey = rankingRedisTemplate.generateDailyRankingKey(date);
        String member = productId.toString();

        Long rank = rankingRedisTemplate.getReverseRank(rankingKey, member);

        if (rank == null) {
            return null;
        }

        return rank + 1;
    }
}
