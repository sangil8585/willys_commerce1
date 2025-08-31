package com.loopers.domain.coupon;

import com.loopers.domain.coupon.CouponEntity;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    
    CouponEntity save(CouponEntity coupon);
    
    Optional<CouponEntity> findById(Long id);
    
    Optional<CouponEntity> findByIdWithLock(Long id);
    
    List<CouponEntity> findByUserId(Long userId);
    
    List<CouponEntity> findByUserIdAndIsUsedFalse(Long userId);
    
    List<CouponEntity> findByUserIdAndIsUsedFalseAndExpiredAtAfter(Long userId);
    
    void deleteById(Long id);
} 