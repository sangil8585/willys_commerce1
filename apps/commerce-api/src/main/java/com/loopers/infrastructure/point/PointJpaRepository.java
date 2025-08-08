package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {
    
    Optional<PointEntity> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    // 포인트를 차감할때 비관적 락을 사용한 포인트 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointEntity p WHERE p.userId = :userId")
    Optional<PointEntity> findByUserIdWithLock(@Param("userId") String userId);
    
    // 포인트 충전시 낙관적 락을 사용한 포인트 조회
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM PointEntity p WHERE p.userId = :userId")
    Optional<PointEntity> findByUserIdWithOptimisticLock(@Param("userId") String userId);
} 