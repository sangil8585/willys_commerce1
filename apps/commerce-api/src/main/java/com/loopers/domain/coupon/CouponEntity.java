package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CouponType type;
    
    @Column(name = "discount_value", nullable = false)
    private Long discountValue;
    
    @Column(name = "min_order_amount")
    private Long minOrderAmount;
    
    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;
    
    @Column(name = "expired_at", nullable = false)
    private ZonedDateTime expiredAt;
    
    @Column(name = "used_at")
    private ZonedDateTime usedAt;
    
    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    public CouponEntity(String userId, String name, CouponType type, Long discountValue, 
                       Long minOrderAmount, Long maxDiscountAmount, ZonedDateTime expiredAt) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.expiredAt = expiredAt;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public CouponType getType() {
        return type;
    }

    public Long getDiscountValue() {
        return discountValue;
    }

    public Long getMinOrderAmount() {
        return minOrderAmount;
    }

    public Long getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public ZonedDateTime getExpiredAt() {
        return expiredAt;
    }

    public ZonedDateTime getUsedAt() {
        return usedAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiredAt);
    }

    public boolean canUse(Long orderAmount) {
        if (isUsed || isExpired()) {
            return false;
        }
        
        if (minOrderAmount != null && orderAmount < minOrderAmount) {
            return false;
        }
        
        return true;
    }

    public Long calculateDiscount(Long orderAmount) {
        if (!canUse(orderAmount)) {
            return 0L;
        }

        Long discount = 0L;
        
        if (type == CouponType.FIXED_AMOUNT) {
            discount = discountValue;
        } else if (type == CouponType.PERCENTAGE) {
            discount = (orderAmount * discountValue) / 100;
        }
        
        // 최대 할인 금액 제한
        if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            discount = maxDiscountAmount;
        }
        
        // 주문 금액을 초과하지 않도록
        if (discount > orderAmount) {
            discount = orderAmount;
        }
        
        return discount;
    }

    public void use() {
        if (isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        
        this.isUsed = true;
        this.usedAt = ZonedDateTime.now();
    }

    public static CouponEntity create(String userId, String name, CouponType type, Long discountValue, 
                                    Long minOrderAmount, Long maxDiscountAmount, ZonedDateTime expiredAt) {
        return new CouponEntity(userId, name, type, discountValue, minOrderAmount, maxDiscountAmount, expiredAt);
    }
} 