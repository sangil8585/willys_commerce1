package com.loopers.application.order;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private CouponEntity testCoupon;
    private static final Long TEST_POINT = 10000L;

    @BeforeEach
    void setUp() {
        UserCommand.Create createCommand = new UserCommand.Create(
                "sangil8585",
                UserEntity.Gender.MALE,
                "1993-02-24",
                "sangil8585@naver.com"
        );
        userInfo = userFacade.signUp(createCommand);

        // 충분한 포인트 충전
        pointService.charge(userInfo.userId(), TEST_POINT);

        // 테스트용 상품 생성 (재고: 10개, 가격: 1000원)
        var productCommand = new com.loopers.domain.product.ProductCommand.Create("테스트 상품", 1L, 1000L, 10L, 0L);
        testProduct = productService.createProduct(productCommand);

        // 테스트용 쿠폰 생성 (100원 할인)
        testCoupon = couponService.createCoupon(
            userInfo.userId(),
            "테스트 쿠폰",
            CouponType.FIXED_AMOUNT,
            100L,
            500L,
            100L,
            ZonedDateTime.now().plusDays(1)
        );
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다")
    void 동시주문_재고차감_테스트() throws Exception {
        // given
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<OrderCommand.OrderItem> items = List.of(
                        OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
                    );
                    OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);
                    
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failureCount.incrementAndGet();
                }
            }, executor);
            futures.add(future);
        }

        // 모든 요청 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        
        assertThat(updatedProduct.getStock()).isEqualTo(10L - successCount.get());
        
        assertThat(successCount.get() + failureCount.get()).isEqualTo(threadCount);
        
        assertThat(updatedProduct.getStock()).isGreaterThanOrEqualTo(0L);
        
        assertThat(successCount.get()).isGreaterThan(0);
        assertThat(failureCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다")
    void 동시주문_쿠폰사용_테스트() throws Exception {
        // given
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<OrderCommand.OrderItem> items = List.of(
                        OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
                    );
                    OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items, testCoupon.getId());
                    
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failureCount.incrementAndGet();
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        CouponEntity usedCoupon = couponService.findById(testCoupon.getId()).orElseThrow();
        assertThat(usedCoupon.isUsed()).isTrue();
        
        // 성공한 주문은 1개, 실패한 주문은 4개
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(4);
    }

    @Test
    @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다")
    void 동시주문_포인트차감_테스트() throws Exception {
        // given
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Long initialPoints = pointService.get(userInfo.userId()).orElse(0L);

        // when
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<OrderCommand.OrderItem> items = List.of(
                        OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
                    );
                    OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);
                    
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    failureCount.incrementAndGet();
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // then
        Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
        Long expectedPoints = initialPoints - (successCount.get() * 1000L);
        assertThat(finalPoints).isEqualTo(expectedPoints);
        
        assertThat(successCount.get() + failureCount.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("동일한 상품에 대해 여러명이 좋아요/싫어요를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다")
    void 동시_좋아요_싫어요_테스트() throws Exception {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ProductEntity product = productService.findByIdWithLockForLikes(testProduct.getId()).orElseThrow();
                    if (index % 2 == 0) {
                        product.incrementLikes();
                    } else {
                        product.decrementLikes();
                    }
                    productService.save(product);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getLikes()).isEqualTo(0L);
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문 전체 흐름에 대해 원자성이 보장되어야 한다 - 하나라도 실패하면 모두 롤백")
    void 주문_원자성_테스트() {
        // given 
        Long currentPoints = pointService.get(userInfo.userId()).orElse(0L);
        pointService.charge(userInfo.userId(), -currentPoints);
        pointService.charge(userInfo.userId(), 500L); 

        // when & then
        List<OrderCommand.OrderItem> items = List.of(
            OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
        );
        OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);

        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(10L);

        Long remainingPoints = pointService.get(userInfo.userId()).orElse(0L);
        assertThat(remainingPoints).isEqualTo(500L);
    }

    @Test
    @DisplayName("사용 불가능하거나 존재하지 않는 쿠폰일 경우 주문은 실패해야 한다")
    void 사용불가능한_쿠폰_주문실패_테스트() {
        // given - 존재하지 않는 쿠폰 ID
        List<OrderCommand.OrderItem> items = List.of(
            OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
        );
        OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items, 99999L); 

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);

        // 재고가 차감되지 않았는지 확인
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(10L);
    }

    @Test
    @DisplayName("재고가 존재하지 않거나 부족할 경우 주문은 실패해야 한다")
    void 재고부족_주문실패_테스트() {
        // given
        List<OrderCommand.OrderItem> items = List.of(
            OrderCommand.OrderItem.of(testProduct.getId(), 15, testProduct.getPrice())
        );
        OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);

        // 재고가 차감되지 않았는지 확인
        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(10L);
    }

    @Test
    @DisplayName("주문 시 유저의 포인트 잔액이 부족할 경우 주문은 실패해야 한다")
    void 포인트부족_주문실패_테스트() {
        // given 
        Long currentPoints = pointService.get(userInfo.userId()).orElse(0L);
        pointService.charge(userInfo.userId(), -currentPoints); 
        pointService.charge(userInfo.userId(), 500L); 

        List<OrderCommand.OrderItem> items = List.of(
            OrderCommand.OrderItem.of(testProduct.getId(), 1, testProduct.getPrice())
        );
        OrderCommand.Create command = OrderCommand.Create.of(userInfo.id(), items);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);

        ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(10L);

        Long remainingPoints = pointService.get(userInfo.userId()).orElse(0L);
        assertThat(remainingPoints).isEqualTo(500L);
    }
} 
