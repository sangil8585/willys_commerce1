package com.loopers.infrastructure.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * WeightRedisTemplate의 RedisTemplate 기반 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeightRedisTemplateImpl implements WeightRedisTemplate {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public double getWeight(String key, double defaultValue) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Double.parseDouble(value);
            }
        } catch (Exception e) {
            log.error("Weight 조회 실패 - key: {}, error: {}", key, e.getMessage());
        }
        return defaultValue;
    }

    @Override
    public void setWeight(String key, double value) {
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(value));
            log.debug("Weight 설정 완료 - key: {}, value: {}", key, value);
        } catch (Exception e) {
            log.error("Weight 설정 실패 - key: {}, value: {}, error: {}",
                    key, value, e.getMessage());
            throw e;
        }
    }
}
