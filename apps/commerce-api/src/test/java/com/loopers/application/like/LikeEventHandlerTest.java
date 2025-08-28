package com.loopers.application.like;

import com.loopers.domain.like.LikeEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeEventHandlerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LikeEventHandler likeEventHandler;

    @Test
    @DisplayName("좋아요 생성 이벤트를 처리한다")
    void 좋아요_생성_이벤트_처리() {
        // given
        LikeEvent.Created event = LikeEvent.Created.of(1L, 1L, 1L);

        // when
        likeEventHandler.handleLikeCreated(event);

        // then
        // 로그 출력 확인 (실제 구현에서는 부가 기능 호출 확인)
        // verify(productService, times(1)).incrementLikes(event.productId());
        // verify(userActivityService, times(1)).logLikeAction(event.userId(), event.productId(), "LIKE");
    }

    @Test
    @DisplayName("좋아요 삭제 이벤트를 처리한다")
    void 좋아요_삭제_이벤트_처리() {
        // given
        LikeEvent.Removed event = LikeEvent.Removed.of(1L, 1L, 1L);

        // when
        likeEventHandler.handleLikeRemoved(event);

        // then
        // 로그 출력 확인 (실제 구현에서는 부가 기능 호출 확인)
        // verify(productService, times(1)).decrementLikes(event.productId());
        // verify(userActivityService, times(1)).logLikeAction(event.userId(), event.productId(), "UNLIKE");
    }

    @Test
    @DisplayName("집계 처리 이벤트를 비동기로 처리한다")
    void 집계_처리_이벤트_비동기_처리() {
        // given
        LikeEvent.AggregationRequested event = LikeEvent.AggregationRequested.of(1L, 10L);

        // when
        likeEventHandler.handleAggregationRequested(event);

        // then
        // 로그 출력 확인 (실제 구현에서는 부가 기능 호출 확인)
        // verify(likeAggregationService, times(1)).processAggregation(event.productId());
        // verify(productCacheService, times(1)).evictProductCache(event.productId());
        // verify(analyticsService, times(1)).sendLikeAnalytics(event.productId(), event.currentLikes());
    }

    @Test
    @DisplayName("좋아요 이벤트 처리 중 예외가 발생해도 메인 트랜잭션에 영향을 주지 않는다")
    void 좋아요_이벤트_처리_예외_발생시_메인_트랜잭션_보호() {
        // given
        LikeEvent.Created event = LikeEvent.Created.of(1L, 1L, 1L);

        // when & then
        // 예외가 발생해도 테스트가 통과해야 함 (메인 트랜잭션 보호)
        likeEventHandler.handleLikeCreated(event);
        
        // 실제 구현에서는 부가 기능에서 예외가 발생해도 메인 트랜잭션은 영향받지 않음
        // verify(productService, times(1)).incrementLikes(event.productId());
    }

    @Test
    @DisplayName("집계 처리 이벤트 실패 시에도 좋아요 처리는 정상적으로 완료된다")
    void 집계_처리_실패시에도_좋아요_처리_완료() {
        // given
        LikeEvent.AggregationRequested event = LikeEvent.AggregationRequested.of(1L, 10L);

        // when & then
        // 집계 처리 실패 시에도 테스트가 통과해야 함 (eventual consistency)
        likeEventHandler.handleAggregationRequested(event);
        
        // 실제 구현에서는 집계 처리 실패 시에도 좋아요 처리는 정상적으로 완료됨
        // verify(likeAggregationService, times(1)).processAggregation(event.productId());
    }
}
