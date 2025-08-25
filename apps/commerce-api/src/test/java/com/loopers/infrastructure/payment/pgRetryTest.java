package com.loopers.infrastructure.payment;

import com.loopers.resilience.AbstractResilienceTest;
import com.loopers.support.resilience.ResilienceConstant;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class pgRetryTest extends AbstractResilienceTest {

    private static final Logger log = LoggerFactory.getLogger(pgRetryTest.class);

    @Test
    @DisplayName("PG_RETRY가 정상적으로 등록되어야 한다")
    public void PG_RETRY가_정상적으로_등록되어야_한다() {
        // Then
        assertThat(retryRegistry.retry(ResilienceConstant.PG_RETRY)).isNotNull();
    }

    @DisplayName("PG_RETRY 테스트")
    @Nested
    class PgRetryTest {
        
        @Test
        @DisplayName("실제 등록된 PG_RETRY의 설정을 확인해야 한다")
        public void 실제_등록된_PG_RETRY의_설정을_확인해야_한다() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_RETRY);
            var config = retry.getRetryConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_RETRY 실제 설정 ===");
            log.info("config 타입: {}", config.getClass().getName());
            log.info("config: {}", config);
            log.info("==========================");
            
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("재시도 없이 성공하는 경우 테스트")
        public void 재시도_없이_성공하는_경우_테스트() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_RETRY);
            
            // When
            String result = retry.executeSupplier(() -> "success");
            
            // Then
            assertThat(result).isEqualTo("success");
            assertThat(getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_RETRY)).isEqualTo(1.0f);
            assertThat(getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_RETRY)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("1번 재시도 후 성공하는 경우 테스트")
        public void 한번_재시도_후_성공하는_경우_테스트() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_RETRY);
            final int[] attemptCount = {0};
            
            // When
            String result = retry.executeSupplier(() -> {
                attemptCount[0]++;
                if (attemptCount[0] == 1) {
                    throw new RuntimeException("첫 번째 시도 실패");
                }
                return "success after retry";
            });
            
            // Then
            assertThat(result).isEqualTo("success after retry");
            assertThat(attemptCount[0]).isEqualTo(2);
            assertThat(getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_RETRY)).isEqualTo(0.0f);
            assertThat(getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_RETRY)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과로 실패하는 경우 테스트")
        public void 최대_재시도_횟수_초과로_실패하는_경우_테스트() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_RETRY);
            final int[] attemptCount = {0};
            
            // When & Then
            assertThatThrownBy(() -> {
                retry.executeSupplier(() -> {
                    attemptCount[0]++;
                    throw new RuntimeException("시도 " + attemptCount[0] + " 실패");
                });
            }).isInstanceOf(RuntimeException.class)
              .hasMessage("시도 3 실패");
            
            // 최대 재시도 횟수(3)만큼 시도했는지 확인
            assertThat(attemptCount[0]).isEqualTo(3);
            assertThat(getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_RETRY)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("지수 백오프가 적용되는지 테스트")
        public void 지수_백오프가_적용되는지_테스트() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_RETRY);
            final int[] attemptCount = {0};
            final long[] attemptTimes = {0, 0, 0};
            
            // When
            try {
                retry.executeSupplier(() -> {
                    attemptCount[0]++;
                    attemptTimes[attemptCount[0] - 1] = System.currentTimeMillis();
                    
                    if (attemptCount[0] < 3) {
                        throw new RuntimeException("시도 " + attemptCount[0] + " 실패");
                    }
                    return "success";
                });
            } catch (Exception e) {
                // 예외는 무시하고 시간 측정만 확인
            }
            
            // Then
            assertThat(attemptCount[0]).isEqualTo(3);
            
            // 지수 백오프가 적용되었는지 확인 (두 번째와 세 번째 시도 사이의 간격이 더 길어야 함)
            if (attemptTimes[1] > 0 && attemptTimes[2] > 0) {
                long firstInterval = attemptTimes[1] - attemptTimes[0];
                long secondInterval = attemptTimes[2] - attemptTimes[1];
                
                log.info("첫 번째 간격: {}ms", firstInterval);
                log.info("두 번째 간격: {}ms", secondInterval);
                
                // 두 번째 간격이 첫 번째 간격보다 길어야 함 (지수 백오프 적용)
                assertThat(secondInterval).isGreaterThan(firstInterval);
            }
        }

        @Test
        @DisplayName("재시도 메트릭이 정확히 기록되는지 테스트")
        public void 재시도_메트릭이_정확히_기록되는지_테스트() {
            // Given
            Retry retry = retryRegistry.retry(ResilienceConstant.PG_RETRY);
            
            // 메트릭 초기화
            log.info("테스트 전 메트릭 - 성공: {}, 실패: {}", 
                    getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_RETRY),
                    getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_RETRY));
            
            // When - 성공 케이스
            String successResult = retry.executeSupplier(() -> "success");
            
            // When - 실패 케이스 (최대 재시도 후 실패)
            try {
                retry.executeSupplier(() -> {
                    throw new RuntimeException("항상 실패");
                });
            } catch (Exception e) {
                // 예외는 무시
            }
            
            // Then
            assertThat(successResult).isEqualTo("success");
            
            // 메트릭 확인
            float successCount = getCurrentCount(SUCCESS_WITHOUT_RETRY, ResilienceConstant.PG_RETRY);
            float failureCount = getCurrentCount(FAILED_WITH_RETRY, ResilienceConstant.PG_RETRY);
            
            log.info("테스트 후 메트릭 - 성공: {}, 실패: {}", successCount, failureCount);
            
            // 성공은 1번, 실패는 1번 기록되어야 함
            assertThat(successCount).isEqualTo(1.0f);
            assertThat(failureCount).isEqualTo(1.0f);
        }
    }
}
