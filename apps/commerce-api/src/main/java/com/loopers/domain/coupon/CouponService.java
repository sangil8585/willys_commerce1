package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponService {
    
    private final CouponRepository couponRepository;
    
    @Transactional
    public CouponEntity createCoupon(
            String userId,
            String name,
            CouponType type,
            Long discountValue,
            Long minOrderAmount,
            Long maxDiscountAmount,
            ZonedDateTime expiredAt) {
        CouponEntity coupon = CouponEntity.create(
                userId,
                name,
                type,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                expiredAt
        );
        return couponRepository.save(coupon);
    }
    
    @Transactional
    public CouponEntity useCoupon(Long couponId, String userId, Long orderAmount) {
        // 비관적 락으로 쿠폰 조회 (다른 트랜잭션이 대기)
        CouponEntity coupon = couponRepository.findByIdWithLock(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        
        // 사용자 소유권 확인
        if (!coupon.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 쿠폰의 소유자가 아닙니다.");
        }
        
        // 사용 가능 여부 확인
        if (!coupon.canUse(orderAmount)) {
            if (coupon.isUsed()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
            } else if (coupon.isExpired()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰입니다.");
            } else {
                throw new CoreException(ErrorType.BAD_REQUEST, "최소 주문 금액을 만족하지 않습니다.");
            }
        }
        
        // 쿠폰 사용 처리 (비관적 락으로 동시성 제어)
        coupon.use();
        return couponRepository.save(coupon);
    }
    
    @Transactional(readOnly = true)
    public Long calculateDiscount(Long couponId, String userId, Long orderAmount) {
        CouponEntity coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        
        // 사용자 소유권 확인
        if (!coupon.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "해당 쿠폰의 소유자가 아닙니다.");
        }
        
        return coupon.calculateDiscount(orderAmount);
    }
    
    @Transactional(readOnly = true)
    public Optional<CouponEntity> findById(Long id) {
        return couponRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<CouponEntity> findByUserId(String userId) {
        return couponRepository.findByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public List<CouponEntity> findAvailableCoupons(String userId) {
        return couponRepository.findByUserIdAndIsUsedFalseAndExpiredAtAfter(userId);
    }
    
    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
}
