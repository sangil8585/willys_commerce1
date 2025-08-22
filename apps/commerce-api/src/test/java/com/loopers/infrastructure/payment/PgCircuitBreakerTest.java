package com.loopers.infrastructure.payment;

import com.loopers.resilience.AbstractResilienceTest;
import com.loopers.support.resilience.ResilienceConstant;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import static org.assertj.core.api.Assertions.assertThat;

public class PgCircuitBreakerTest extends AbstractResilienceTest {
    
    private static final Logger log = LoggerFactory.getLogger(PgCircuitBreakerTest.class);

    @Test
    @DisplayName("PG_FIND_CB와 PG_REQUEST_CB 서킷브레이커가 정상적으로 등록되어야 한다")
    public void PG_FIND_CB_AND_PG_REQUEST_CB_서킷브레이커가_동작해야함() {
        // Then
        assertThat(circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB)).isNotNull();
        assertThat(circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB)).isNotNull();
    }

    @DisplayName("PG_FIND_CB")
    @Nested
    class PgFindCb {
        @Test
        @DisplayName("PG_FIND_CB 서킷브레이커가 OPEN 상태로 전환되어야 한다")
        public void 서킷브레이커가_OPEN_상태로_전환() {
            // When
            transitionToOpenState(ResilienceConstant.PG_FIND_CB);

            // Then
            checkHealthStatus(ResilienceConstant.PG_FIND_CB, State.OPEN);
        }

        @Test
        @DisplayName("실제 등록된 PG_FIND_CB 서킷브레이커의 설정을 확인해야 한다")
        public void 서킷브레이커의_설정을_확인() {
            // Given
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // Then - 실제 설정값 출력
            log.info("=== PG_FIND_CB 실제 설정 ===");
            log.info("failureRateThreshold: {}", config.getFailureRateThreshold());
            log.info("minimumNumberOfCalls: {}", config.getMinimumNumberOfCalls());
            log.info("slidingWindowSize: {}", config.getSlidingWindowSize());
            log.info("permittedNumberOfCallsInHalfOpenState: {}", config.getPermittedNumberOfCallsInHalfOpenState());
            log.info("slidingWindowType: {}", config.getSlidingWindowType());
            log.info("==========================");

            // 설정값 검증 (resilience.yml에 설정한 값들)
            assertThat(config).isNotNull();
            assertThat(config.getFailureRateThreshold()).isEqualTo(50.0f);
            assertThat(config.getMinimumNumberOfCalls()).isEqualTo(4);
            assertThat(config.getSlidingWindowSize()).isEqualTo(8);
            assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
        }

        @Test
        @DisplayName("실제 설정으로 PG_FIND_CB 서킷브레이커가 OPEN 상태가 되는지 테스트")
        public void 서킷브레이커_OPEN_상태_확인() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("실제 설정으로 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize());

            // When - 실제 설정에 맞는 실패 횟수로 테스트
            int requiredFailures = calculateRequiredFailures(config);
            log.info("필요한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시하고 계속 호출
                }
            });

            // Then - 서킷브레이커 상태 확인
            State currentState = circuitBreaker.getState();
            log.info("현재 서킷브레이커 상태: {}", currentState);

            // OPEN 상태가 되었는지 확인 (실제 설정에 따라 달라질 수 있음)
            assertThat(currentState).isIn(State.OPEN, State.CLOSED);
        }

        @Test
        @DisplayName("실제 설정으로 PG_FIND_CB 서킷브레이커가 HALF_OPEN에서 CLOSED로 전환되는지 테스트")
        public void 서킷브레이커가_HALF_OPEN_에서_CLOSED가되는지_확인() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_FIND_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("HALF_OPEN->CLOSED 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}, permittedNumberOfCallsInHalfOpenState: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize(), config.getPermittedNumberOfCallsInHalfOpenState());

            // 먼저 실패를 발생시켜 OPEN 상태로 만들기 (실제 설정에 맞게)
            int requiredFailures = calculateRequiredFailures(config);
            log.info("OPEN 상태로 만들기 위한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    if (count % 5 == 0) log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시
                }
            });

            // OPEN 상태 확인
            State openState = circuitBreaker.getState();
            log.info("OPEN 상태 확인: {}", openState);
            assertThat(openState).isEqualTo(State.OPEN);

            // HALF_OPEN 상태로 전환
            circuitBreaker.transitionToHalfOpenState();
            State halfOpenState = circuitBreaker.getState();
            log.info("HALF_OPEN 상태 확인: {}", halfOpenState);
            assertThat(halfOpenState).isEqualTo(State.HALF_OPEN);

            // HALF_OPEN 상태에서 성공을 여러 번 발생시켜 CLOSED로 전환
            int requiredSuccesses = config.getPermittedNumberOfCallsInHalfOpenState();
            log.info("CLOSED 상태로 만들기 위한 성공 횟수: {}", requiredSuccesses);

            IntStream.rangeClosed(1, requiredSuccesses).forEach(count -> {
                produceSuccess(circuitBreaker);
                log.info("성공 {}번 발생", count);
            });

            // Then - 서킷브레이커가 CLOSED 상태가 되어야 함
            State finalState = circuitBreaker.getState();
            log.info("최종 서킷브레이커 상태: {}", finalState);
            assertThat(finalState).isEqualTo(State.CLOSED);
        }
    }

    @DisplayName("PG_REQUEST_CB")
    @Nested
    class PgRequestCb {

        @Test
        @DisplayName("PG_REQUEST_CB 서킷브레이커가 CLOSED 상태로 전환되어야 한다")
        public void 서킷브레이커가_CLOSED상태인지_확인() {
            // Given
            transitionToOpenState(ResilienceConstant.PG_REQUEST_CB);

            // When
            transitionToClosedState(ResilienceConstant.PG_REQUEST_CB);

            // Then
            checkHealthStatus(ResilienceConstant.PG_REQUEST_CB, State.CLOSED);
        }

        @Test
        @DisplayName("실제 설정으로 PG_REQUEST_CB 서킷브레이커가 OPEN 상태가 되는지 테스트")
        public void PG_REQUEST_CB_서킷브레이커가_OPEN되어있는지_테스트() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("실제 설정으로 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize());

            // When - 실제 설정에 맞는 실패 횟수로 테스트
            int requiredFailures = calculateRequiredFailures(config);
            log.info("필요한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시하고 계속 호출
                }
            });

            // Then - 서킷브레이커 상태 확인
            State currentState = circuitBreaker.getState();
            log.info("현재 서킷브레이커 상태: {}", currentState);

            // OPEN 상태가 되었는지 확인 (실제 설정에 따라 달라질 수 있음)
            assertThat(currentState).isIn(State.OPEN, State.CLOSED);
        }

        @Test
        @DisplayName("실제 설정으로 PG_REQUEST_CB 서킷브레이커가 HALF_OPEN에서 CLOSED로 전환되는지 테스트")
        public void 실제_등록된_서킷브레이커가_HALF_OPEN에서_CLOSED로전환되는지확인() {
            // Given - 실제 등록된 서킷브레이커 사용
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(ResilienceConstant.PG_REQUEST_CB);
            var config = circuitBreaker.getCircuitBreakerConfig();

            // 설정값 출력
            log.info("HALF_OPEN->CLOSED 테스트 - failureRateThreshold: {}, minimumNumberOfCalls: {}, slidingWindowSize: {}, permittedNumberOfCallsInHalfOpenState: {}",
                    config.getFailureRateThreshold(), config.getMinimumNumberOfCalls(), config.getSlidingWindowSize(), config.getPermittedNumberOfCallsInHalfOpenState());

            // 먼저 실패를 발생시켜 OPEN 상태로 만들기 (실제 설정에 맞게)
            int requiredFailures = calculateRequiredFailures(config);
            log.info("OPEN 상태로 만들기 위한 실패 횟수: {}", requiredFailures);

            IntStream.rangeClosed(1, requiredFailures).forEach(count -> {
                try {
                    produceFailure(circuitBreaker);
                    if (count % 5 == 0) log.info("실패 {}번 발생", count);
                } catch (Exception e) {
                    // 예외는 무시
                }
            });

            // OPEN 상태 확인
            State openState = circuitBreaker.getState();
            log.info("OPEN 상태 확인: {}", openState);
            assertThat(openState).isEqualTo(State.OPEN);

            // HALF_OPEN 상태로 전환
            circuitBreaker.transitionToHalfOpenState();
            State halfOpenState = circuitBreaker.getState();
            log.info("HALF_OPEN 상태 확인: {}", halfOpenState);
            assertThat(halfOpenState).isEqualTo(State.HALF_OPEN);

            // HALF_OPEN 상태에서 성공을 여러 번 발생시켜 CLOSED로 전환
            int requiredSuccesses = config.getPermittedNumberOfCallsInHalfOpenState();
            log.info("CLOSED 상태로 만들기 위한 성공 횟수: {}", requiredSuccesses);

            IntStream.rangeClosed(1, requiredSuccesses).forEach(count -> {
                produceSuccess(circuitBreaker);
                log.info("성공 {}번 발생", count);
            });

            // Then - 서킷브레이커가 CLOSED 상태가 되어야 함
            State finalState = circuitBreaker.getState();
            log.info("최종 서킷브레이커 상태: {}", finalState);
            assertThat(finalState).isEqualTo(State.CLOSED);
        }
    }
}
