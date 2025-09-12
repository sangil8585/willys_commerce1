package com.loopers.infrastructure.ranking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public interface RankingRedisTemplate {

    void incrementScore(String key, String member, double score);

    Set<String> getReverseRange(String key, long start, long end);

    Long getSize(String key);

    void setTTL(String key, long seconds);

    Long getReverseRank(String key, String member);

    default String generateDailyRankingKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
