package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("사용자 ID로 쿠폰을 조회할 수 있어야 한다")
    void 사용자별_쿠폰_조회_테스트() {
        // given
        String userId = "sangil8585";
        couponService.createCoupon(userId, "1000원 할인 쿠폰", CouponType.FIXED_AMOUNT, 1000L, 5000L, null, ZonedDateTime.now().plusDays(7));
        couponService.createCoupon(userId, "10% 할인 쿠폰", CouponType.PERCENTAGE, 10L, 5000L, null, ZonedDateTime.now().plusDays(7));

        // when
        List<CouponEntity> userCoupons = couponService.findByUserId(userId);

        // then
        assertThat(userCoupons).hasSize(2);
        assertThat(userCoupons).allMatch(coupon -> coupon.getUserId().equals(userId));
    }

    @Test
    @DisplayName("사용 가능한 쿠폰만 조회할 수 있어야 한다")
    void 사용가능한_쿠폰_조회_테스트() {
        // given
        String userId = "sangil8585";
        // 사용 가능한 쿠폰
        CouponEntity validCoupon = couponService.createCoupon(userId, "유효한 쿠폰", CouponType.FIXED_AMOUNT, 1000L, 5000L, null, ZonedDateTime.now().plusDays(7));
        // 만료된 쿠폰
        couponService.createCoupon(
                userId,
                "만료된 쿠폰",
                CouponType.FIXED_AMOUNT,
                1000L,
                5000L,
                null,
                ZonedDateTime.now().minusDays(1)
        );
        // 사용된 쿠폰
        CouponEntity usedCoupon = couponService.createCoupon(userId, "사용된 쿠폰", CouponType.FIXED_AMOUNT, 1000L, 5000L, null, ZonedDateTime.now().plusDays(7));
        couponService.useCoupon(usedCoupon.getId(), userId, 10000L);

        // when
        List<CouponEntity> availableCoupons = couponService.findAvailableCoupons(userId);

        // then
        assertThat(availableCoupons).hasSize(1);
        assertThat(availableCoupons.get(0).getId()).isEqualTo(validCoupon.getId());
    }

    @Test
    @DisplayName("정률 할인 쿠폰의 할인 금액을 계산할 수 있어야 한다")
    void 정률_할인_쿠폰_계산_테스트() {
        // given
        String userId = "sangil8585";
        CouponEntity coupon = couponService.createCoupon(
                userId, "정률 쿠폰",
                CouponType.PERCENTAGE,
                15L,  // 15퍼 할인 쿠폰
                5000L,
                null,
                ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 10000L;

        // when
        Long discountAmount = couponService.calculateDiscount(coupon.getId(), userId, orderAmount);

        // then
        assertThat(discountAmount).isEqualTo(1500L); // 10000원 x 0.15 = 1500원
    }

    @Test
    @DisplayName("쿠폰을 사용할 수 있어야 한다")
    void 쿠폰_사용_테스트() {
        // given
        String userId = "sangil8585";
        CouponEntity coupon = couponService.createCoupon(
                userId,
                "1000원 할인 쿠폰",
                CouponType.FIXED_AMOUNT,
                1000L,
                5000L,
                null,
                ZonedDateTime.now().plusDays(7)
        );
        Long orderAmount = 10000L;

        // when
        CouponEntity usedCoupon = couponService.useCoupon(coupon.getId(), userId, orderAmount);

        // then
        assertThat(usedCoupon.isUsed()).isTrue();
        assertThat(usedCoupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰을 사용하려고 하면 예외가 발생해야 한다")
    void 존재하지_않는_쿠폰_사용_예외_테스트() {
        // given
        String userId = "sangil8585";
        Long nonExistentCouponId = 999L;
        Long orderAmount = 10000L;

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(nonExistentCouponId, userId, orderAmount))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("쿠폰을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하려고 하면 예외가 발생해야 한다")
    void 이미_사용된_쿠폰_재사용_예외_테스트() {
        // given
        String userId = "sangil8585";
        CouponEntity coupon = couponService.createCoupon(userId, "만원 쿠폰", CouponType.FIXED_AMOUNT, 1000L, 5000L, null, ZonedDateTime.now().plusDays(7));
        Long orderAmount = 10000L;

        // 첫 번째 사용
        couponService.useCoupon(coupon.getId(), userId, orderAmount);

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), userId, orderAmount))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("이미 사용된 쿠폰입니다");
    }

    @Test
    @DisplayName("만료된 쿠폰을 사용하려고 하면 예외가 발생해야 한다")
    void 만료된_쿠폰_사용_예외_테스트() {
        // given
        String userId = "sangil8585";
        CouponEntity coupon = couponService.createCoupon(userId, "만원 쿠폰", CouponType.FIXED_AMOUNT, 1000L, 5000L, null, ZonedDateTime.now().minusDays(1));
        Long orderAmount = 10000L;

        // when & then
        assertThatThrownBy(() -> couponService.useCoupon(coupon.getId(), userId, orderAmount))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("만료된 쿠폰입니다");
    }

    @Test
    @DisplayName("쿠폰 ID로 쿠폰을 조회할 수 있어야 한다")
    void 쿠폰_ID_조회_테스트() {
        // given
        String userId = "sangil8585";
        CouponEntity createdCoupon = couponService.createCoupon(
                userId,
                "1000원 할인 쿠폰",
                CouponType.FIXED_AMOUNT,
                1000L,
                5000L,
                null,
                ZonedDateTime.now().plusDays(7)
        );

        // when
        Optional<CouponEntity> foundCoupon = couponService.findById(createdCoupon.getId());

        // then
        assertThat(foundCoupon).isPresent();
        assertThat(foundCoupon.get().getId()).isEqualTo(createdCoupon.getId());
        assertThat(foundCoupon.get().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 ID로 조회하면 빈 Optional을 반환해야 한다")
    void 존재하지_않는_쿠폰_조회_테스트() {
        // given
        Long nonExistentCouponId = 999L;

        // when
        Optional<CouponEntity> foundCoupon = couponService.findById(nonExistentCouponId);

        // then
        assertThat(foundCoupon).isEmpty();
    }
}
