package com.loopers.application.order;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private PointService pointService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private UserInfo userInfo;
    private ProductEntity testProduct;
    private Long couponId;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        String loginId = "concurrency_test_user";
        String gender = "MALE";
        String birthDate = "1990-01-01";
        String email = "concurrency@test.com";

        var userCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        userInfo = userFacade.signUp(userCommand);

        // 포인트 충전 (충분한 금액)
        pointService.charge(userInfo.userId(), 100000L);

        // 테스트용 상품 생성 (재고 10개)
        var productCommand = new com.loopers.domain.product.ProductCommand.Create("테스트 상품", 1L, 1000L, 10L, 0L);
        testProduct = productService.createProduct(productCommand);

        // 테스트용 쿠폰 생성
        couponId = couponService.createCoupon(
            userInfo.userId(),
            "테스트 쿠폰",
            CouponType.FIXED_AMOUNT,
            100L,
            1000L,
            null,
            ZonedDateTime.now().plusDays(1)
        ).getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 상품에 대한 동시 주문 시 재고가 정상적으로 차감된다")
    @Test
    void 동시주문_재고차감_테스트() throws Exception {
        // given
        int threadCount = 5;
        int orderQuantity = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when - 동시에 주문 요청
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<OrderCommand.OrderItem> items = List.of(
                        OrderCommand.OrderItem.of(testProduct.getId(), orderQuantity, testProduct.getPrice())
                    );
                    OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);
                    
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            }, executor);
            futures.add(future);
        }

        // 모든 요청 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        // 재고가 정확히 차감되었는지 확인 (10개 - (성공한 주문 수 * 2개))
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        int expectedStock = 10 - (successCount.get() * orderQuantity);
        assertEquals(expectedStock, updatedProduct.getStock());
        
        // 실패한 주문이 있어야 함 (재고 부족으로)
        assertThat(failureCount.get()).isGreaterThan(0);
    }

    @DisplayName("동일한 쿠폰으로 동시 주문 시 쿠폰은 한 번만 사용된다")
    @Test
    void 동시주문_쿠폰사용_테스트() throws Exception {
        // given
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when - 동시에 같은 쿠폰으로 주문
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<OrderCommand.OrderItem> items = List.of(
                        OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
                    );
                    OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items, couponId);
                    
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            }, executor);
            futures.add(future);
        }

        // 모든 요청 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        // 쿠폰은 한 번만 사용되어야 함
        assertEquals(1, successCount.get());
        assertEquals(threadCount - 1, failureCount.get());
    }

    @DisplayName("동일한 사용자의 동시 주문 시 포인트가 정상적으로 차감된다")
    @Test
    void 동시주문_포인트차감_테스트() throws Exception {
        // given
        int threadCount = 3;
        int orderAmount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // 초기 포인트 확인
        Long initialPoints = pointService.get(userInfo.userId()).orElse(0L);

        // when - 동시에 주문
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<OrderCommand.OrderItem> items = List.of(
                        OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
                    );
                    OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);
                    
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 실패는 무시
                }
            }, executor);
            futures.add(future);
        }

        // 모든 요청 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        // 포인트가 정확히 차감되었는지 확인
        Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
        Long expectedPoints = initialPoints - (successCount.get() * orderAmount);
        assertEquals(expectedPoints, finalPoints);
    }

    @DisplayName("동시 좋아요/싫어요 시 상품의 좋아요 개수가 정상 반영된다")
    @Test
    void 동시_좋아요_싫어요_테스트() throws Exception {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // when - 동시에 좋아요/싫어요 요청
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ProductEntity product = productService.findById(testProduct.getId()).orElseThrow();
                    if (index % 2 == 0) {
                        product.incrementLikes();
                    } else {
                        product.decrementLikes();
                    }
                    productService.save(product);
                } catch (Exception e) {
                    // 예외 무시
                }
            }, executor);
            futures.add(future);
        }

        // 모든 요청 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        // 최종 좋아요 개수 확인 (5개 증가 - 5개 감소 = 0개 변화)
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertEquals(0L, updatedProduct.getLikes());
    }
} 