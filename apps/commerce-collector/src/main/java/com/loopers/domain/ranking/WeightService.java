package com.loopers.domain.ranking;

import com.loopers.infrastructure.ranking.WeightRedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Weight 관리를 위한 도메인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeightService {

    private final WeightRedisTemplate weightRedisTemplate;

    private static final double DEFAULT_VIEW_WEIGHT = 0.1;
    private static final double DEFAULT_LIKE_WEIGHT = 0.2;
    private static final double DEFAULT_ORDER_WEIGHT = 0.6;

    public double getViewWeight() {
        return getWeight("view", DEFAULT_VIEW_WEIGHT);
    }

    public double getLikeWeight() {
        return getWeight("like", DEFAULT_LIKE_WEIGHT);
    }

    public double getOrderWeight() {
        return getWeight("order", DEFAULT_ORDER_WEIGHT);
    }

    public void setViewWeight(double weight) {
        setWeight("view", weight);
    }

    public void setLikeWeight(double weight) {
        setWeight("like", weight);
    }

    public void setOrderWeight(double weight) {
        setWeight("order", weight);
    }

    public void resetToDefaults() {
        setViewWeight(DEFAULT_VIEW_WEIGHT);
        setLikeWeight(DEFAULT_LIKE_WEIGHT);
        setOrderWeight(DEFAULT_ORDER_WEIGHT);
    }

    private double getWeight(String eventType, double defaultValue) {
        String key = weightRedisTemplate.generateWeightKey(eventType);
        return weightRedisTemplate.getWeight(key, defaultValue);
    }

    private void setWeight(String eventType, double value) {
        String key = weightRedisTemplate.generateWeightKey(eventType);
        weightRedisTemplate.setWeight(key, value);
    }
}
