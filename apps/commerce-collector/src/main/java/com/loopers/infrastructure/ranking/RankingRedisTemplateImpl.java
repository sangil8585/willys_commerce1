package com.loopers.infrastructure.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingRedisTemplateImpl implements RankingRedisTemplate {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void incrementScore(String key, String member, double score) {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            Double currentScore = zSetOps.incrementScore(key, member, score);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Set<String> getReverseRange(String key, long start, long end) {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            return zSetOps.reverseRange(key, start, end);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Long getSize(String key) {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            return zSetOps.zCard(key);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void setTTL(String key, long seconds) {
        try {
            Boolean result = redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("TTL 설정 실패 - key: {}, seconds: {}, error: {}",
                    key, seconds, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Long getReverseRank(String key, String member) {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            return zSetOps.reverseRank(key, member);

        } catch (Exception e) {
            log.error("랭킹 순위 조회 실패 - key: {}, member: {}, error: {}",
                    key, member, e.getMessage(), e);
            throw e;
        }
    }
}
