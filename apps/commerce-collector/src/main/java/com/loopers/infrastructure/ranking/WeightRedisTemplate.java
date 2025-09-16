package com.loopers.infrastructure.ranking;

public interface WeightRedisTemplate {

    double getWeight(String key, double defaultValue);

    void setWeight(String key, double value);

    default String generateWeightKey(String eventType) {
        return "ranking:weights:" + eventType;
    }
}
