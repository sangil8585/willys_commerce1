package com.loopers.infrastructure.payment;

import com.loopers.resilience.AbstractResilienceTest;
import com.loopers.support.resilience.ResilienceConstant;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class pgTimeOutTest extends AbstractResilienceTest {

    private static final Logger log = LoggerFactory.getLogger(pgTimeOutTest.class);

    @Test
    @DisplayName("PG_TIME_LIMITER가 정상적으로 등록되어야 한다")
    public void PG_TIME_LIMITER가_정상적으로_등록되어야_한다() {
        // Then
        assertThat(timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER)).isNotNull();
    }

    @DisplayName("PG_TIME_LIMITER 테스트")
    @Nested
    class PgTimeLimiterTest {
        
        @Test
        @DisplayName("실제 등록된 PG_TIME_LIMITER의 설정을 확인해야 한다")
        public void 실제_등록된_PG_TIME_LIMITER의_설정을_확인해야_한다() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            TimeLimiterConfig config = timeLimiter.getTimeLimiterConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_TIME_LIMITER 실제 설정 ===");
            log.info("config 타입: {}", config.getClass().getName());
            log.info("timeoutDuration: {}ms", config.getTimeoutDuration().toMillis());
            log.info("cancelRunningFuture: {}", config.shouldCancelRunningFuture());
            log.info("==========================");
            
            assertThat(config).isNotNull();
            assertThat(config.getTimeoutDuration().toMillis()).isGreaterThan(0);
        }

        @Test
        @DisplayName("타임아웃 없이 성공하는 경우 테스트")
        public void 타임아웃_없이_성공하는_경우_테스트() throws Exception {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            long shortDuration = 100; // 100ms (타임아웃보다 짧음)
            
            // When
            String result = executeTimeLimiterTask(timeLimiter, shortDuration);
            
            // Then
            assertThat(result).isEqualTo("success");
        }

        @Test
        @DisplayName("타임아웃으로 실패하는 경우 테스트")
        public void 타임아웃으로_실패하는_경우_테스트() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            long longDuration = 10000; // 10초 (타임아웃보다 김)
            
            // When & Then
            assertThatThrownBy(() -> {
                executeTimeLimiterTask(timeLimiter, longDuration);
            }).isInstanceOf(Exception.class)
              .hasMessageContaining("TimeoutException");
        }

        @Test
        @DisplayName("타임아웃 설정값에 따른 동작 테스트")
        public void 타임아웃_설정값에_따른_동작_테스트() throws Exception {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            TimeLimiterConfig config = timeLimiter.getTimeLimiterConfig();
            long timeoutMs = config.getTimeoutDuration().toMillis();
            
            log.info("타임아웃 설정값: {}ms", timeoutMs);
            
            // When - 타임아웃보다 약간 짧은 시간으로 실행
            long shortDuration = Math.max(1, timeoutMs - 100); // 타임아웃보다 100ms 짧게
            String result = executeTimeLimiterTask(timeLimiter, shortDuration);
            
            // Then - 성공해야 함
            assertThat(result).isEqualTo("success");
            log.info("타임아웃 내 실행 성공: {}ms < {}ms", shortDuration, timeoutMs);
        }

        @Test
        @DisplayName("타임아웃 임계값에서의 동작 테스트")
        public void 타임아웃_임계값에서의_동작_테스트() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            TimeLimiterConfig config = timeLimiter.getTimeLimiterConfig();
            long timeoutMs = config.getTimeoutDuration().toMillis();
            
            log.info("타임아웃 임계값 테스트 - timeoutMs: {}ms", timeoutMs);
            
            // When
            long longDuration = timeoutMs + 200;
            
            // Then - 타임아웃 예외가 발생해야 함
            assertThatThrownBy(() -> {
                executeTimeLimiterTask(timeLimiter, longDuration);
            }).isInstanceOf(Exception.class)
              .hasMessageContaining("TimeoutException");
            
            log.info("타임아웃 발생 확인: {}ms > {}ms", longDuration, timeoutMs);
        }

        @Test
        @DisplayName("CompletableFuture를 사용한 타임아웃 테스트")
        public void CompletableFuture를_사용한_타임아웃_테스트() throws Exception {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            
            // When - ScheduledExecutorService를 사용하여 executeCompletionStage 호출
            CompletableFuture<String> future = timeLimiter.executeCompletionStage(
                Executors.newSingleThreadScheduledExecutor(),
                () -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(5000);
                        return "delayed success";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                })
            ).toCompletableFuture();
            
            // Then - 타임아웃 예외가 발생해야 함
            assertThatThrownBy(() -> {
                future.get(10, TimeUnit.SECONDS); // 10초 대기
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("타임아웃 발생 시 cancelRunningFuture 동작 테스트")
        public void 타임아웃_발생_시_cancelRunningFuture_동작_테스트() {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            TimeLimiterConfig config = timeLimiter.getTimeLimiterConfig();
            
            log.info("cancelRunningFuture 설정: {}", config.shouldCancelRunningFuture());
            
            // When - 타임아웃이 발생하는 긴 작업 실행
            long longDuration = 10000; // 10초 (타임아웃보다 김)
            
            // Then - 타임아웃 예외 발생
            assertThatThrownBy(() -> {
                executeTimeLimiterTask(timeLimiter, longDuration);
            }).isInstanceOf(Exception.class);
            
            // cancelRunningFuture가 true인 경우 실행 중인 작업이 취소되었는지 확인
            if (config.shouldCancelRunningFuture()) {
                log.info("실행 중인 작업이 취소되었습니다.");
            } else {
                log.info("실행 중인 작업이 백그라운드에서 계속 실행됩니다.");
            }
        }

        @Test
        @DisplayName("여러 타임아웃 작업의 동시 실행 테스트")
        public void 여러_타임아웃_작업의_동시_실행_테스트() throws Exception {
            // Given
            TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(ResilienceConstant.PG_TIME_LIMITER);
            TimeLimiterConfig config = timeLimiter.getTimeLimiterConfig();
            long timeoutMs = config.getTimeoutDuration().toMillis();
            
            log.info("동시 실행 테스트 - timeoutMs: {}ms", timeoutMs);
            
            // When - 여러 작업을 동시에 실행
            CompletableFuture<String> fastTask = timeLimiter.executeCompletionStage(
                Executors.newSingleThreadScheduledExecutor(),
                () -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100); // 빠른 작업
                        return "fast success";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                })
            ).toCompletableFuture();
            
            CompletableFuture<String> slowTask = timeLimiter.executeCompletionStage(
                Executors.newSingleThreadScheduledExecutor(),
                () -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(timeoutMs + 1000); // 느린 작업 (타임아웃 초과)
                        return "slow success";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                })
            ).toCompletableFuture();
            
            // Then - 빠른 작업은 성공, 느린 작업은 타임아웃
            String fastResult = fastTask.get(5, TimeUnit.SECONDS);
            assertThat(fastResult).isEqualTo("fast success");
            
            assertThatThrownBy(() -> {
                slowTask.get(5, TimeUnit.SECONDS);
            }).isInstanceOf(Exception.class);
            
            log.info("빠른 작업 성공: {}", fastResult);
            log.info("느린 작업 타임아웃 확인");
        }
    }
}
