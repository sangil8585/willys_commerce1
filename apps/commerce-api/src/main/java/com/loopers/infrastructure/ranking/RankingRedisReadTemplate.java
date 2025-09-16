package com.loopers.infrastructure.ranking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public interface RankingRedisReadTemplate {

    Set<String> getReverseRange(String key, long start, long end);
    Long getSize(String key);
    Long getReverseRank(String key, String member);
    default String generateDailyRankingKey(LocalDate date) {
        return "ranking:all:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
