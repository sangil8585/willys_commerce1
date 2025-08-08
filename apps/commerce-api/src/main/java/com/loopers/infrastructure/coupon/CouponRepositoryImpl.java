package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CouponRepositoryImpl implements CouponRepository {
    
    private final CouponJpaRepository couponJpaRepository;
    
    @Override
    public CouponEntity save(CouponEntity coupon) {
        return couponJpaRepository.save(coupon);
    }
    
    @Override
    public Optional<CouponEntity> findById(Long id) {
        return couponJpaRepository.findById(id);
    }
    
    @Override
    public List<CouponEntity> findByUserId(String userId) {
        return couponJpaRepository.findByUserId(userId);
    }
    
    @Override
    public List<CouponEntity> findByUserIdAndIsUsedFalse(String userId) {
        return couponJpaRepository.findByUserIdAndIsUsedFalse(userId);
    }
    
    @Override
    public List<CouponEntity> findByUserIdAndIsUsedFalseAndExpiredAtAfter(String userId) {
        return couponJpaRepository.findByUserIdAndIsUsedFalseAndExpiredAtAfter(userId);
    }
    
    @Override
    public void deleteById(Long id) {
        couponJpaRepository.deleteById(id);
    }
    
    @Override
    public Optional<CouponEntity> findByIdWithLock(Long id) {
        return couponJpaRepository.findByIdWithLock(id);
    }
} 