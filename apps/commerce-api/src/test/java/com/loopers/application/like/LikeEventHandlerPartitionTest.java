package com.loopers.application.like;

import com.loopers.config.kafka.KafkaEventPublisher;
import com.loopers.domain.like.LikeEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductService;
import com.loopers.event.like.LikeKafkaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeEventHandlerPartitionTest {

    @Mock
    private LikeService likeService;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private LikeEventHandler likeEventHandler;

    @BeforeEach
    void setUp() {
        likeEventHandler = new LikeEventHandler(
            likeService, productService, kafkaEventPublisher
        );
    }

    @Test
    void 좋아요_생성_이벤트_발행시_productId가_파티션키로_사용된다() {
        // given
        Long userId = 1L;
        Long productId = 12345L;
        
        LikeEvent.Created event = LikeEvent.Created.of(userId, productId);

        // when
        likeEventHandler.handleLikeCreatedAfterCommit(event);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LikeKafkaEvent> eventCaptor = ArgumentCaptor.forClass(LikeKafkaEvent.class);

        verify(kafkaEventPublisher).publishEventAsync(
            topicCaptor.capture(), 
            keyCaptor.capture(), 
            eventCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("like-events");
        assertThat(keyCaptor.getValue()).isEqualTo(productId.toString());
        assertThat(eventCaptor.getValue()).isNotNull();
    }

    @Test
    void 좋아요_삭제_이벤트_발행시_productId가_파티션키로_사용된다() {
        // given
        Long userId = 1L;
        Long productId = 12345L;
        
        LikeEvent.Removed event = LikeEvent.Removed.of(userId, productId);

        // when
        likeEventHandler.handleLikeRemovedAfterCommit(event);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LikeKafkaEvent> eventCaptor = ArgumentCaptor.forClass(LikeKafkaEvent.class);

        verify(kafkaEventPublisher).publishEventAsync(
            topicCaptor.capture(), 
            keyCaptor.capture(), 
            eventCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo("like-events");
        assertThat(keyCaptor.getValue()).isEqualTo(productId.toString());
        assertThat(eventCaptor.getValue()).isNotNull();
    }

    @Test
    void 동일한_productId를_가진_여러_이벤트가_같은_파티션키를_사용한다() {
        // given
        Long productId = 12345L;
        Long userId1 = 1L;
        Long userId2 = 2L;
        
        LikeEvent.Created createdEvent = LikeEvent.Created.of(userId1, productId);
        LikeEvent.Removed removedEvent = LikeEvent.Removed.of(userId2, productId);

        // when
        likeEventHandler.handleLikeCreatedAfterCommit(createdEvent);
        likeEventHandler.handleLikeRemovedAfterCommit(removedEvent);

        // then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaEventPublisher, times(2)).publishEventAsync(
            any(), keyCaptor.capture(), any()
        );

        List<String> capturedKeys = keyCaptor.getAllValues();
        assertThat(capturedKeys).hasSize(2);
        assertThat(capturedKeys.get(0)).isEqualTo(productId.toString());
        assertThat(capturedKeys.get(1)).isEqualTo(productId.toString());
    }

    @Test
    void 서로_다른_productId를_가진_이벤트는_다른_파티션키를_사용한다() {
        // given
        Long productId1 = 12345L;
        Long productId2 = 67890L;
        Long userId = 1L;
        
        LikeEvent.Created event1 = LikeEvent.Created.of(userId, productId1);
        LikeEvent.Created event2 = LikeEvent.Created.of(userId, productId2);

        // when
        likeEventHandler.handleLikeCreatedAfterCommit(event1);
        likeEventHandler.handleLikeCreatedAfterCommit(event2);

        // then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaEventPublisher, times(2)).publishEventAsync(
            any(), keyCaptor.capture(), any()
        );

        List<String> capturedKeys = keyCaptor.getAllValues();
        assertThat(capturedKeys).hasSize(2);
        assertThat(capturedKeys.get(0)).isEqualTo(productId1.toString());
        assertThat(capturedKeys.get(1)).isEqualTo(productId2.toString());
        assertThat(capturedKeys.get(0)).isNotEqualTo(capturedKeys.get(1));
    }
}
