package com.loopers.application.point;

import com.loopers.domain.user.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PointFacade {

    private final PointService pointService;

    @Transactional
    public PointInfo chargePoint(String userId, Long amount) {
        Long resultAmount = pointService.charge(userId, amount);
        return PointInfo.from(userId, resultAmount);
    }

    @Transactional(readOnly = true)
    public PointInfo getPointInfo(String userId) {
        Long amount = pointService.get(userId);
        return PointInfo.from(userId, amount, false);
    }
} 