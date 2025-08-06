package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointService {
    private final PointRepository pointRepository;

    public Optional<Long> get(String userId) {
        return pointRepository.getPointByUserId(userId);
    }

    @Transactional
    public Long charge(String userId, Long amount) {
        return pointRepository.chargePoint(userId, amount);
    }
    
    // 비관락을 사용해서 포인트 차감하는 메서드
    @Transactional
    public void deductPoint(String userId, Long amount) {
        // 비관적 락으로 포인트 조회. 다른 트랜잭션이 끝날때까지 대기한다.
        PointEntity point = pointRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 포인트 정보를 찾을 수 없습니다."));
        
        if (point.getAmount() < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        
        // 포인트를 차감한다.
        point.charge(-amount);
        pointRepository.save(point);
    }
    
    public void createPointForUser(String userId) {
        pointRepository.createPointForUser(userId);
    }
} 
