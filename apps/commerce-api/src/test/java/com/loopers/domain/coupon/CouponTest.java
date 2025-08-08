package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    @DisplayName("쿠폰 생성 시 기본값이 올바르게 설정되어야 한다")
    void 쿠폰_생성_테스트() {
        // given
        String userId = "sangil8585";
        String name = "갓 태어난 쿠폰";
        CouponType type = CouponType.FIXED_AMOUNT;
        Long discountValue = 1000L;
        Long minOrderAmount = 5000L;
        Long maxDiscountAmount = 2000L;
        ZonedDateTime expiredAt = ZonedDateTime.now().plusDays(7);

        // when
        CouponEntity coupon = CouponEntity.create(userId, name, type, discountValue, minOrderAmount, maxDiscountAmount, expiredAt);

        // then
        assertThat(coupon.getUserId()).isEqualTo(userId);
        assertThat(coupon.getName()).isEqualTo(name);
        assertThat(coupon.getType()).isEqualTo(type);
        assertThat(coupon.getDiscountValue()).isEqualTo(discountValue);
        assertThat(coupon.getMinOrderAmount()).isEqualTo(minOrderAmount);
        assertThat(coupon.getMaxDiscountAmount()).isEqualTo(maxDiscountAmount);
        assertThat(coupon.getExpiredAt()).isEqualTo(expiredAt);
        assertThat(coupon.isUsed()).isFalse();
        assertThat(coupon.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("정액 할인 쿠폰의 할인 금액이 올바르게 계산되어야 한다")
    void 정액_할인_쿠폰_계산_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "1000원 할인 쿠폰",
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, 
            null, 
            ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 10000L;

        // when
        Long discountAmount = coupon.calculateDiscount(orderAmount);

        // then
        assertThat(discountAmount).isEqualTo(1000L);
    }

    @Test
    @DisplayName("정률 할인 쿠폰의 할인 금액이 올바르게 계산되어야 한다")
    void 정률_할인_쿠폰_계산_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "10% 정률할인 쿠폰",
            CouponType.PERCENTAGE, 
            10L,
            5000L, 
            null, 
            ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 10000L;

        // when
        Long discountAmount = coupon.calculateDiscount(orderAmount);

        // then
        assertThat(discountAmount).isEqualTo(1000L); // 10000 * 10% = 1000
    }

    @Test
    @DisplayName("정률 할인 쿠폰에서 최대 할인 금액이 적용되어야 한다")
    void 정률_할인_쿠폰_최대할인금액_제한_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "20% 정률할인 쿠폰",
            CouponType.PERCENTAGE, 
            20L,
            5000L, 
            1500L,
            ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 10000L;

        // when
        Long discountAmount = coupon.calculateDiscount(orderAmount);

        // then
        // 10000 * 20% = 2000이지만, 최대 할인 금액 1500원으로 제한됨
        assertThat(discountAmount).isEqualTo(1500L);
    }

    @Test
    @DisplayName("최소 주문 금액을 만족하지 않으면 쿠폰을 사용할 수 없다")
    void 최소주문금액_미달_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "1000원 쿠폰",
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, // 최소 주문 금액 5000원
            null, 
            ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 3000L; // 최소 주문 금액보다 적음

        // when & then
        assertThat(coupon.canUse(orderAmount)).isFalse();
        assertThat(coupon.calculateDiscount(orderAmount)).isEqualTo(0L);
    }

    @Test
    @DisplayName("만료된 쿠폰은 사용할 수 없다")
    void 만료된_쿠폰_사용불가_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "만료된 쿠폰", 
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, 
            null, 
            ZonedDateTime.now().minusDays(1) // 어제 만료
        );
        Long orderAmount = 10000L;

        // when & then
        assertThat(coupon.isExpired()).isTrue();
        assertThat(coupon.canUse(orderAmount)).isFalse();
        assertThat(coupon.calculateDiscount(orderAmount)).isEqualTo(0L);
    }

    @Test
    @DisplayName("쿠폰 사용 시 사용 상태가 변경되어야 한다")
    void 쿠폰_사용_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "테스트 쿠폰", 
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, 
            null, 
            ZonedDateTime.now().plusDays(7)
        );

        // when
        coupon.use();

        // then
        assertThat(coupon.isUsed()).isTrue();
        assertThat(coupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하려고 하면 예외가 발생해야 한다")
    void 이미_사용된_쿠폰_재사용_예외_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "테스트 쿠폰", 
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, 
            null, 
            ZonedDateTime.now().plusDays(7)
        );
        coupon.use(); // 첫 번째 사용

        // when & then
        assertThatThrownBy(() -> coupon.use())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 사용된 쿠폰입니다.");
    }

    @Test
    @DisplayName("만료된 쿠폰을 사용하려고 하면 예외가 발생해야 한다")
    void 만료된_쿠폰_사용_예외_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "만료된 쿠폰", 
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, 
            null, 
            ZonedDateTime.now().minusDays(1) // 어제 만료
        );

        // when & then
        assertThatThrownBy(() -> coupon.use())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("만료된 쿠폰입니다.");
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 사용할 수 없다")
    void 사용된_쿠폰_사용불가_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "테스트 쿠폰", 
            CouponType.FIXED_AMOUNT, 
            1000L, 
            5000L, 
            null, 
            ZonedDateTime.now().plusDays(7)
        );
        coupon.use(); // 쿠폰 사용
        Long orderAmount = 10000L;

        // when & then
        assertThat(coupon.canUse(orderAmount)).isFalse();
        assertThat(coupon.calculateDiscount(orderAmount)).isEqualTo(0L);
    }

    @Test
    @DisplayName("할인 금액이 주문 금액을 초과하지 않아야 한다")
    void 할인금액_주문금액_초과_방지_테스트() {
        // given
        CouponEntity coupon = CouponEntity.create(
            "sangil8585",
            "테스트 쿠폰", 
            CouponType.FIXED_AMOUNT, 
            5000L, // 할인 금액 5000원
            1000L, 
            null, 
            ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 3000L; // 주문 금액 3000원

        // when
        Long discountAmount = coupon.calculateDiscount(orderAmount);

        // then
        // 할인 금액(5000원)이 주문 금액(3000원)을 초과하므로 주문 금액만큼만 할인
        assertThat(discountAmount).isEqualTo(3000L);
    }
}
