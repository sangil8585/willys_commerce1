package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingPeriod;
import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingRedisReadTemplate rankingRedisReadTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> getRankedProducts(int offset, int size, LocalDate date, RankingPeriod period) {
        if (period == RankingPeriod.DAILY) {
            String rankingKey = rankingRedisReadTemplate.generateDailyRankingKey(date);
            Set<String> productIds = rankingRedisReadTemplate.getReverseRange(rankingKey, offset, offset + size - 1);
            return productIds.stream().map(Long::parseLong).collect(Collectors.toList());
        }

        String table = period == RankingPeriod.WEEKLY ? "mv_product_rank_weekly" : "mv_product_rank_monthly";
        String sql = "SELECT product_id FROM " + table + " WHERE as_of_date = ? ORDER BY rank ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("product_id"), date, size, offset);
    }

    @Override
    public Long getTotalCount(LocalDate date, RankingPeriod period) {
        if (period == RankingPeriod.DAILY) {
            String rankingKey = rankingRedisReadTemplate.generateDailyRankingKey(date);
            Long count = rankingRedisReadTemplate.getSize(rankingKey);
            return count != null ? count : 0L;
        }
        String table = period == RankingPeriod.WEEKLY ? "mv_product_rank_weekly" : "mv_product_rank_monthly";
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE as_of_date = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, date);
        return count != null ? count : 0L;
    }

    @Override
    public Long getProductRank(Long productId, LocalDate date, RankingPeriod period) {
        if (period == RankingPeriod.DAILY) {
            String rankingKey = rankingRedisReadTemplate.generateDailyRankingKey(date);
            String member = productId.toString();
            Long rank = rankingRedisReadTemplate.getReverseRank(rankingKey, member);
            return rank != null ? rank + 1 : null;
        }

        String table = period == RankingPeriod.WEEKLY ? "mv_product_rank_weekly" : "mv_product_rank_monthly";
        String sql = "SELECT rank FROM " + table + " WHERE as_of_date = ? AND product_id = ?";
        List<Long> ranks = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("rank"), date, productId);
        return ranks.isEmpty() ? null : ranks.get(0);
    }
}
