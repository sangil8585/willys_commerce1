package com.loopers.infrastructure.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingRedisTemplateImpl implements RankingRedisTemplate {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Set<String> getReverseRange(String key, long start, long end) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRange(key, start, end);
    }

    @Override
    public Long getSize(String key) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.zCard(key);
    }

    @Override
    public Long getReverseRank(String key, String member) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRank(key, member);
    }
}
