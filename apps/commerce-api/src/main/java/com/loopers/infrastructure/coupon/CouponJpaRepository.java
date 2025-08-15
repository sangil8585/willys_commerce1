package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
    
    List<CouponEntity> findByUserId(String userId);
    
    List<CouponEntity> findByUserIdAndIsUsedFalse(String userId);
    
    @Query("SELECT c FROM CouponEntity c WHERE c.userId = :userId AND c.isUsed = false AND c.expiredAt > :now")
    List<CouponEntity> findByUserIdAndIsUsedFalseAndExpiredAtAfter(@Param("userId") String userId, @Param("now") ZonedDateTime now);
    
    default List<CouponEntity> findByUserIdAndIsUsedFalseAndExpiredAtAfter(String userId) {
        return findByUserIdAndIsUsedFalseAndExpiredAtAfter(userId, ZonedDateTime.now());
    }
    
    // 비관적 락을 사용한 쿠폰 조회 (사용 처리용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id = :id")
    Optional<CouponEntity> findByIdWithLock(@Param("id") Long id);
} 