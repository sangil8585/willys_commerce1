package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointService {
    private final PointRepository pointRepository;

    public Long get(String userId) {
        return pointRepository.getPointByUserId(userId);
    }

    public Long charge(String userId, Long amount) {
        return pointRepository.chargePoint(userId, amount);
    }
    
    public void createPointForUser(String userId) {
        pointRepository.createPointForUser(userId);
    }
} 
