package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<Long> getPointByUserId(String userId);
    // Long chargePoint(String userId, Long amount);
    void createPointForUser(String userId);
    PointEntity save(PointEntity point);
    Optional<PointEntity> findByUserIdWithLock(String userId);
    Optional<PointEntity> findByUserIdWithOptimisticLock(String userId);
} 