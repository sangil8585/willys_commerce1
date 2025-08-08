package com.loopers.application.point;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PointFacadeIntegrationTest {
    @Autowired
    private PointFacade pointFacade;
    @Autowired
    private PointService pointService;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private UserInfo userInfo;
    private static final Long TEST_POINT = 10000L;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @BeforeEach
    void setUp() {
        UserCommand.Create createCommand = new UserCommand.Create(
                "sangil8585",
                UserEntity.Gender.MALE,
                "1993-02-24",
                "sangil8585@naver.com"
        );
        userInfo = userFacade.signUp(createCommand);

        pointFacade.chargePoint(userInfo.userId(), TEST_POINT);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /**
     * - [x]  해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.
     * - [x]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     *
     */
    @DisplayName("포인트조회")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void 회원이_존재할경우_보유포인트_반환() {
            // given

            // when
            PointInfo pointInfo = pointFacade.getPointInfo(userInfo.userId());

            // then
            assertNotNull(pointInfo);
            assertNotNull(pointInfo.amount());
            assertEquals(TEST_POINT, pointInfo.amount());
            assertTrue(!(pointInfo.amount() < 0));
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void 아이디가_존재하지않는_경우_null반환() {
            // given
            String nonExist = "nonExist";

            // when
            PointInfo pointInfo = pointFacade.getPointInfo(nonExist);

            // then
            assertNull(pointInfo);
        }
    }

    /**
     * - [x]  존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.
     */
    @DisplayName("포인트충전")
    @Nested
    class Charge {
        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void 존재하지_않는_유저로_충전시_실패() {
            // given
            String nonExist = "nonExist";
            Long amount = 100L;

            // when
            CoreException coreException = assertThrows(CoreException.class, () -> {
                pointFacade.chargePoint(nonExist, amount);
            });

            // then - 존재하지 않는 사용자는 404 NOT_FOUND가 적절
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("포인트차감")
    @Nested
    class Deduction {
        @DisplayName("동시 포인트 차감 시 정합성 보장")
        @Test
        void 동시_포인트_차감_정합성_테스트() throws Exception {
            // given
            int threadCount = 10;
            Long deductAmount = 1000L;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when - 동시에 포인트 차감 (초기 10000원, 10명이 1000원씩 차감)
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        pointService.deductPoint(userInfo.userId(), deductAmount);
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

            // then - 포인트가 정확히 0원이 되어야 함
            Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
            assertEquals(0L, finalPoints);
            assertEquals(10, successCount.get());
            assertEquals(0, failureCount.get());
        }

        @DisplayName("포인트 부족 시 동시 차감 요청 처리")
        @Test
        void 포인트_부족시_동시_차감_요청_테스트() throws Exception {
            // given
            int threadCount = 15;
            Long deductAmount = 1000L;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when - 재고보다 많은 차감 요청 (초기 10000원, 15명이 1000원씩 차감)
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        pointService.deductPoint(userInfo.userId(), deductAmount);
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

            // then - 정확히 10번만 성공하고 나머지는 실패해야 함
            Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
            assertEquals(0L, finalPoints);
            assertEquals(10, successCount.get());
            assertEquals(5, failureCount.get());
        }

        @DisplayName("낙관적 락을 사용한 동시 포인트 충전 시 정합성 보장")
        @Test
        void 낙관적락_동시_포인트_충전_정합성_테스트() throws Exception {
            // given
            int threadCount = 10;
            Long chargeAmount = 1000L;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when - 동시에 포인트 충전 (낙관적 락 사용)
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        pointService.charge(userInfo.userId(), chargeAmount);
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
            Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
            Long expectedPoints = TEST_POINT + (successCount.get() * chargeAmount);

            LOGGER.info("Charge Attribute. [finalPoints={}, expectedPoints={}, successCount={}, failureCount={}]", 
                       finalPoints, expectedPoints, successCount.get(), failureCount.get());

            assertEquals(expectedPoints, finalPoints);
            assertEquals(threadCount, successCount.get() + failureCount.get());
        }

        @DisplayName("존재하지 않는 사용자에 대한 동시 차감 요청")
        @Test
        void 존재하지_않는_사용자_동시_차감_테스트() throws Exception {
            // given
            String nonExistentUserId = "non_existent_user";
            int threadCount = 5;
            Long deductAmount = 1000L;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger failureCount = new AtomicInteger(0);

            // when - 존재하지 않는 사용자에 대해 동시 차감 요청
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        pointService.deductPoint(nonExistentUserId, deductAmount);
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                }, executor);
                futures.add(future);
            }

            // 모든 요청 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // then - 모든 요청이 실패해야 함
            assertEquals(threadCount, failureCount.get());
        }

        @DisplayName("다양한 금액으로 동시 차감 요청")
        @Test
        void 다양한_금액_동시_차감_테스트() throws Exception {
            // given
            int threadCount = 5;
            Long[] deductAmounts = {500L, 1000L, 1500L, 2000L, 2500L}; // 총 7500원
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when - 서로 다른 금액으로 동시 차감
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        pointService.deductPoint(userInfo.userId(), deductAmounts[index]);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                }, executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // then - 모든 차감이 성공하고 남은 포인트 확인
            Long finalPoints = pointService.get(userInfo.userId()).orElse(0L);
            Long expectedRemaining = TEST_POINT - 7500L; // 10000 - 7500 = 2500
            assertEquals(expectedRemaining, finalPoints);
            assertEquals(5, successCount.get());
            assertEquals(0, failureCount.get());
        }

        @DisplayName("동시 포인트 조회 시 일관성 보장")
        @Test
        void 동시_포인트_조회_일관성_테스트() throws Exception {
            // given
            int threadCount = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Long>> futures = new ArrayList<>();

            // when - 동시에 포인트 조회
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    return pointService.get(userInfo.userId()).orElse(0L);
                }, executor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // then - 모든 조회 결과가 동일해야 함
            Long expectedPoints = TEST_POINT;
            for (CompletableFuture<Long> future : futures) {
                assertEquals(expectedPoints, future.get());
            }
        }
    }
} 
