package com.loopers.domain.point;

public interface PointRepository {
    Long getPointByUserId(String userId);
    Long chargePoint(String userId, Long amount);
    void createPointForUser(String userId);
} 