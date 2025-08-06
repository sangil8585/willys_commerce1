package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<Long> getPointByUserId(String userId) {
        Optional<PointEntity> optional = pointJpaRepository.findByUserId(userId);
        return optional.map(PointEntity::getAmount);
    }

    @Override
    public Long chargePoint(String userId, Long amount) {
        Optional<PointEntity> optional = pointJpaRepository.findByUserId(userId);
        if(optional.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        PointEntity pointEntity = optional.get();
        pointEntity.charge(amount);
        PointEntity saved = pointJpaRepository.save(pointEntity);

        return saved.getAmount();
    }

    @Override
    public void createPointForUser(String userId) {
        if (!pointJpaRepository.existsByUserId(userId)) {
            PointEntity pointEntity = PointEntity.create(userId);
            pointJpaRepository.save(pointEntity);
        }
    }
    
    // 비관락 메서드 추가가
    public Optional<PointEntity> findByUserIdWithLock(String userId) {
        return pointJpaRepository.findByUserIdWithLock(userId);
    }
    
    @Override
    public PointEntity save(PointEntity point) {
        return pointJpaRepository.save(point);
    }
} 