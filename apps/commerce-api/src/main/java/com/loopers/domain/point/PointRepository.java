package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<Long> getPointByUserId(Long userId);
    // Long chargePoint(String userId, Long amount);
    void createPointForUser(Long userId);
    PointEntity save(PointEntity point);
    Optional<PointEntity> findByUserIdWithLock(Long userId);
    Optional<PointEntity> findByUserIdWithOptimisticLock(Long userId);
} 