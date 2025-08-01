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
    public Long getPointByUserId(String userId) {
        Optional<PointEntity> optional = pointJpaRepository.findByUserId(userId);
        if(optional.isEmpty()) {
            return null;
        }
        PointEntity pointEntity = optional.get();
        return pointEntity.getAmount();
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
} 