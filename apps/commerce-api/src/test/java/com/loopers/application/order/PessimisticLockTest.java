package com.loopers.application.order;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
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
public class PessimisticLockTest {

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
        String loginId = "pessimistic_test_user";
        String gender = "MALE";
        String birthDate = "1990-01-01";
        String email = "pessimistic@test.com";

        var userCommand = UserCommand.Create.of(loginId, gender, birthDate, email);
        userInfo = userFacade.signUp(userCommand);

        pointService.charge(userInfo.userId(), 10000L);

        var productCommand = new ProductCommand.Create("비관적락 테스트 상품", 1L, 1000L, 10L, 0L);
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

    @DisplayName("비관적 락으로 재고 정합성 보장")
    @Test
    void 비관적락_재고_정합성_테스트() throws Exception {
        // given
        int threadCount = 10;
        int orderQuantity = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // when - 동시에 주문 요청 (재고 10개, 10명이 1개씩 주문)
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
                    // 실패처리 어케하지..
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertEquals(0L, updatedProduct.getStock());
        assertEquals(10, successCount.get());
    }

    @DisplayName("비관적 락으로 포인트 정합성 보장")
    @Test
    void 비관적락_포인트_정합성_테스트() throws Exception {
        // given
        int threadCount = 10;
        int orderAmount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // 초기 포인트 확인
        Long initialPoints = pointService.get(userInfo.userId()).orElse(0L);
        assertEquals(10000L, initialPoints);

        // when - 동시에 주문 (포인트 10000원, 10명이 1000원씩 주문)
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

                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then - 포인트가 정확히 0원이 되어야 함
        Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
        assertEquals(0L, finalPoints);
        assertEquals(10, successCount.get());
    }

    @DisplayName("비관적 락으로 쿠폰 중복 사용 방지")
    @Test
    void 비관적락_쿠폰_중복사용_방지_테스트() throws Exception {
        // given
        int threadCount = 5;
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

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then - 쿠폰은 정확히 한 번만 사용되어야 함
        assertEquals(1, successCount.get());
        assertEquals(4, failureCount.get()); // 나머지는 실패
    }

    @DisplayName("비관적 락으로 재고 오버셀 방지")
    @Test
    void 비관적락_재고_오버셀_방지_테스트() throws Exception {
        // given
        int threadCount = 15;
        int orderQuantity = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when - 재고보다 많은 주문 요청 (재고 10개, 15명이 1개씩 주문)
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

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then - 재고가 정확히 0개가 된다. 나머지는 실패한다.
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertEquals(0L, updatedProduct.getStock());
        assertEquals(10, successCount.get());
        assertEquals(5, failureCount.get());
    }
} 