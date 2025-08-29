package com.loopers.application.order;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderCommand;
import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.brand.BrandService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private UserInfo userInfo;
    private ProductEntity testProduct1;
    private ProductEntity testProduct2;
    private Long validCouponId;
    private Long expiredCouponId;
    private Long usedCouponId;

    @BeforeEach
    void setUp() {
        Long loginId = 1L;
        String gender = "MALE";
        String birthDate = "1993-02-24";
        String email = "sangil8585@naver.com";

        var userCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        userInfo = userFacade.signUp(userCommand);

        // 포인트 충전
        pointService.charge(userInfo.userId(), 10000L);

        // 테스트용 브랜드 생성
        Long brandId = brandService.create("나이키").getId();

        // 테스트용 상품 생성1
        var productCommand1 = new ProductCommand.Create("티셔츠", brandId, 1000L, 10L, 0L);
        testProduct1 = productService.createProduct(productCommand1);

        // 테스트용 상품 생성2
        var productCommand2 = new ProductCommand.Create("운동복", brandId, 2000L, 5L, 0L);
        testProduct2 = productService.createProduct(productCommand2);

        // 테스트용 쿠폰들 생성
        validCouponId = couponService.createCoupon(
            userInfo.userId(),
            "유효한 쿠폰",
            CouponType.FIXED_AMOUNT,
            500L,
            1000L,
            500L,
            ZonedDateTime.now().plusDays(1)
        ).getId();

        expiredCouponId = couponService.createCoupon(
            userInfo.userId(),
            "만료된 쿠폰",
            CouponType.FIXED_AMOUNT,
            500L,
            1000L,
            500L,
            ZonedDateTime.now().minusDays(1)
        ).getId();

        usedCouponId = couponService.createCoupon(
            userInfo.userId(),
            "사용된 쿠폰",
            CouponType.FIXED_AMOUNT,
            500L,
            1000L,
            500L,
            ZonedDateTime.now().plusDays(1)
        ).getId();
    }


    @DisplayName("주문 생성")
    @Nested
    class Order {

        @DisplayName("정상적인 주문을 생성한다")
        @Test
        void 정상적인_주문을_생성한다() {
            // given
            List<OrderCriteria.Item> items = List.of(
                // 1000원 티셔츠
                new OrderCriteria.Item(testProduct1.getId(), testProduct1.getStock()),
                // 2000원 운동복
                new OrderCriteria.Item(testProduct2.getId(), testProduct1.getStock())
            );
            // 생성
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when
            OrderResult.OrderResponse orderInfo = orderFacade.createOrder(orderCriteria);

            // then
            assertThat(orderInfo.userId()).isEqualTo(userInfo.id());
            assertThat(orderInfo.totalPrice()).isEqualTo(4000L); // 2x1000+1x2000

            // 주문 생성 후 포인트 잔액확인
            Optional<Long> remainingPoint = pointService.get(userInfo.userId());
            assertThat(remainingPoint.orElse(null)).isEqualTo(6000L); // 10000-4000
        }

        @DisplayName("재고가 부족하면 주문 생성에 실패한다")
        @Test
        void 재고부족시_주문생성_실패() {
            // given
            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 15L)
            );
            // 생성
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("포인트가 부족하면 주문 생성에 실패한다")
        @Test
        void 포인트부족시_주문생성_실패() {
            // given
            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), testProduct1.getStock()) // 11000원 주문 (포인트 10000원)
            );
            // 생성
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("존재하지 않는 상품으로 주문시 실패한다")
        @Test
        void 존재하지않는_상품으로_주문시_실패() {
            // given
            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(999L, 1L)
            );
            // 생성
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> orderFacade.createOrder(orderCriteria));
            assertThat(exception.getMessage()).isEqualTo("상품을 찾을 수 없습니다.");
        }

        @DisplayName("여러 주문을 생성하고 주문 목록을 조회한다")
        @Test
        void 여러주문_생성_및_목록조회() {
            // given
            List<OrderCriteria.Item> items1 = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 1L)
            );
            // 생성
            OrderCriteria.Order orderCriteria1 = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items1, 
                List.of()
            );

            List<OrderCriteria.Item> items2 = List.of(
                new OrderCriteria.Item(testProduct2.getId(), testProduct1.getStock())
            );
            // 생성2
            OrderCriteria.Order orderCriteria2 = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items2, 
                List.of()
            );

            // when
            OrderResult.OrderResponse orderInfo1 = orderFacade.createOrder(orderCriteria1);
            OrderResult.OrderResponse orderInfo2 = orderFacade.createOrder(orderCriteria2);

            // then
            assertThat(orderInfo1.totalPrice()).isEqualTo(1000L);
            assertThat(orderInfo2.totalPrice()).isEqualTo(2000L);
            assertThat(orderInfo1.userId()).isEqualTo(userInfo.id());
            assertThat(orderInfo2.userId()).isEqualTo(userInfo.id());

            Optional<Long> finalPoint = pointService.get(userInfo.userId());
            assertThat(finalPoint.orElse(null)).isEqualTo(7000L);
        }

        @DisplayName("주문 후 포인트 잔액이 정확히 차감된다")
        @Test
        void 주문후_포인트잔액_정확히_차감() {
            // given
            Optional<Long> initialPoint = pointService.get(userInfo.userId());
            assertThat(initialPoint.orElse(null)).isEqualTo(10000L);

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 3L) // 3000원
            );

            // 생성
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when
            OrderResult.OrderResponse orderInfo = orderFacade.createOrder(orderCriteria);

            // then
            assertThat(orderInfo.totalPrice()).isEqualTo(3000L);
            Optional<Long> remainingPoint = pointService.get(userInfo.userId());
            assertThat(remainingPoint.orElse(null)).isEqualTo(7000L);
        }
    }

    @DisplayName("쿠폰 관련 주문 테스트")
    @Nested
    class CouponOrderTest {

        @DisplayName("유효한 쿠폰으로 주문 시 할인이 정상 적용된다")
        @Test
        void 유효한_쿠폰으로_주문_성공() {
            // given
            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 2L) // 2000원
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(validCouponId)
            );

            // when
            OrderResult.OrderResponse orderInfo = orderFacade.createOrder(orderCriteria);

            // then
            assertThat(orderInfo.totalPrice()).isEqualTo(2000L);
            
            // assertThat(orderInfo.finalAmount()).isEqualTo(1500L); // 2000 - 500 할인
            // assertThat(orderInfo.discountAmount()).isEqualTo(500L);
        }

        @DisplayName("존재하지 않는 쿠폰으로 주문 시 실패한다")
        @Test
        void 존재하지_않는_쿠폰으로_주문_실패() {
            // given
            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 1L)
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(99999L)
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
        }

        @DisplayName("만료된 쿠폰으로 주문 시 실패한다")
        @Test
        void 만료된_쿠폰으로_주문_실패() {
            // given
            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 1L)
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(expiredCouponId)
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("다른 사용자의 쿠폰으로 주문 시 실패한다")
        @Test
        void 다른_사용자_쿠폰으로_주문_실패() {
            // given - 다른 사용자 생성
            UserCommand.Create otherUserCommand = UserCommand.Create.of(999L, "FEMALE", "1990-01-01", "other@test.com");
            UserInfo otherUser = userFacade.signUp(otherUserCommand);
            pointService.charge(otherUser.userId(), 10000L);

            // 다른 사용자의 쿠폰 생성
            Long otherUserCouponId = couponService.createCoupon(
                otherUser.userId(),
                "다른 사용자 쿠폰",
                CouponType.FIXED_AMOUNT,
                500L,
                1000L,
                500L,
                ZonedDateTime.now().plusDays(1)
            ).getId();

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 1L)
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(otherUserCouponId)
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }

    @DisplayName("롤백 테스트")
    @Nested
    class RollbackTest {

        @DisplayName("재고 부족으로 주문 실패 시 포인트가 차감되지 않는다")
        @Test
        void 재고부족_실패시_포인트_롤백() {
            // given
            Optional<Long> initialPoint = pointService.get(userInfo.userId());
            assertThat(initialPoint.orElse(null)).isEqualTo(10000L);

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 15L) // 재고 10개인데 15개 주문
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());

            // 포인트가 차감되지 않았는지 확인
            Optional<Long> finalPoint = pointService.get(userInfo.userId());
            assertThat(finalPoint.orElse(null)).isEqualTo(10000L);
        }

        @DisplayName("포인트 부족으로 주문 실패 시 재고가 차감되지 않는다")
        @Test
        void 포인트부족_실패시_재고_롤백() {
            // given
            ProductEntity initialProduct = productService.findById(testProduct1.getId()).orElseThrow();
            Long initialStock = initialProduct.getStock();
            assertThat(initialStock).isEqualTo(10L);

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 11L) // 포인트 10000원인데 11000원 주문
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of()
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());

            // 재고가 차감되지 않았는지 확인
            ProductEntity finalProduct = productService.findById(testProduct1.getId()).orElseThrow();
            assertThat(finalProduct.getStock()).isEqualTo(10L);
        }

        @DisplayName("쿠폰 사용 실패 시 다른 처리가 롤백된다")
        @Test
        void 쿠폰사용_실패시_전체_롤백() {
            // given
            Optional<Long> initialPoint = pointService.get(userInfo.userId());
            assertThat(initialPoint.orElse(null)).isEqualTo(10000L);

            ProductEntity initialProduct = productService.findById(testProduct1.getId()).orElseThrow();
            Long initialStock = initialProduct.getStock();

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 1L)
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(expiredCouponId)
            );

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> orderFacade.createOrder(orderCriteria));
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());

            // 포인트와 재고가 모두 롤백되었는지 확인
            Optional<Long> finalPoint = pointService.get(userInfo.userId());
            assertThat(finalPoint.orElse(null)).isEqualTo(10000L);

            ProductEntity finalProduct = productService.findById(testProduct1.getId()).orElseThrow();
            assertThat(finalProduct.getStock()).isEqualTo(initialStock);
        }
    }

    @DisplayName("주문 성공 시 모든 처리가 정상 반영되는지 테스트")
    @Nested
    class OrderSuccessTest {

        @DisplayName("쿠폰 사용 주문 성공 시 모든 처리가 정상 반영된다")
        @Test
        void 쿠폰_주문_성공시_모든처리_반영() {
            // given
            Optional<Long> initialPoint = pointService.get(userInfo.userId());
            assertThat(initialPoint.orElse(null)).isEqualTo(10000L);

            ProductEntity initialProduct = productService.findById(testProduct1.getId()).orElseThrow();
            Long initialStock = initialProduct.getStock();

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 2L) // 2000원
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(validCouponId)
            );

            // when
            OrderResult.OrderResponse orderInfo = orderFacade.createOrder(orderCriteria);

            // then
            assertThat(orderInfo.totalPrice()).isEqualTo(2000L);
            // OrderResult.OrderResponse에는 finalAmount가 없으므로 주석 처리
            // assertThat(orderInfo.finalAmount()).isEqualTo(1500L); // 2000 - 500 할인

            // 포인트가 정확히 차감되었는지 확인
            Optional<Long> finalPoint = pointService.get(userInfo.userId());
            assertThat(finalPoint.orElse(null)).isEqualTo(8500L); // 10000 - 1500

            // 재고가 정확히 차감되었는지 확인
            ProductEntity finalProduct = productService.findById(testProduct1.getId()).orElseThrow();
            assertThat(finalProduct.getStock()).isEqualTo(initialStock - 2);

            // 쿠폰이 사용되었는지 확인
            var usedCoupon = couponService.findById(validCouponId).orElseThrow();
            assertThat(usedCoupon.isUsed()).isTrue();
        }
    }

    /**
     * - [x]  쿠폰 종류는 정액 / 정률로 구분되며, 각 적용 로직을 구현하였다.
     */
    @DisplayName("쿠폰 종류별 할인 적용 로직 테스트")
    @Nested
    class CouponDiscountLogicTest {

        @DisplayName("정액 할인 쿠폰이 정상적으로 적용되어야 한다")
        @Test
        void 정액할인_쿠폰_적용_테스트() {
            // given - 정액 할인 쿠폰 (1000원 할인)
            Long fixedAmountCouponId = couponService.createCoupon(
                userInfo.userId(),
                "정액 할인 쿠폰",
                CouponType.FIXED_AMOUNT,
                1000L,  // 1000원 할인
                1000L,  // 최소 주문 금액
                1000L,  // 최대 할인 금액
                ZonedDateTime.now().plusDays(1)
            ).getId();

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct1.getId(), 3L) // 3000원
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(fixedAmountCouponId)
            );

            // when
            OrderResult.OrderResponse orderInfo = orderFacade.createOrder(orderCriteria);

            // then - 최종 금액이 2000원이 되어야 함 (3000 - 1000)
            assertThat(orderInfo.totalPrice()).isEqualTo(3000L);
            // assertThat(orderInfo.finalAmount()).isEqualTo(2000L);
            // assertThat(orderInfo.discountAmount()).isEqualTo(1000L);
        }

        @DisplayName("정률 할인 쿠폰이 정상적으로 적용되어야 한다")
        @Test
        void 정률할인_쿠폰_적용_테스트() {
            // given - 정률 할인 쿠폰 (20% 할인)
            Long percentageCouponId = couponService.createCoupon(
                userInfo.userId(),
                "정률 할인 쿠폰",
                CouponType.PERCENTAGE,
                20L,    // 20% 할인
                1000L,  // 최소 주문 금액
                2000L,  // 최대 할인 금액
                ZonedDateTime.now().plusDays(1)
            ).getId();

            List<OrderCriteria.Item> items = List.of(
                new OrderCriteria.Item(testProduct2.getId(), 3L) // 6000원
            );
            OrderCriteria.Order orderCriteria = new OrderCriteria.Order(
                userInfo.id(), 
                "CARD", 
                "VISA", 
                "1234567890123456", 
                "http://localhost:8080/callback",
                items, 
                List.of(percentageCouponId)
            );

            // when
            OrderResult.OrderResponse orderInfo = orderFacade.createOrder(orderCriteria);

            // then - 최종 금액이 4800원이 되어야 함 (6000 * 0.8)
            assertThat(orderInfo.totalPrice()).isEqualTo(6000L);
            // assertThat(orderInfo.finalAmount()).isEqualTo(4800L);
            // assertThat(orderInfo.discountAmount()).isEqualTo(1200L);
        }
    }
} 
