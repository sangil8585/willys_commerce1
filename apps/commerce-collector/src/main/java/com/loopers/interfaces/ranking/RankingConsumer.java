package com.loopers.interfaces.ranking;

import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.ranking.RankingEventService;
import com.loopers.event.like.LikeKafkaEvent;
import com.loopers.event.order.OrderKafkaEvent;
import com.loopers.event.product.ProductViewKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingConsumer {

    private final RankingEventService rankingEventService;

    @KafkaListener(
        topics = "product-view-events",
        groupId = "ranking-group",
        containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleProductViewEvents(
            @Payload List<ProductViewKafkaEvent> events,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        for (ProductViewKafkaEvent event : events) {
            rankingEventService.handleProductView(event.getProductId());
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(
        topics = "like-events",
        groupId = "ranking-group",
        containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeEvents(
            @Payload List<LikeKafkaEvent> events,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        for (LikeKafkaEvent event : events) {
            boolean isCreated = "CREATED".equals(event.getAction());
            rankingEventService.handleLike(event.getProductId(), isCreated);
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(
        topics = "order-events",
        groupId = "ranking-group",
        containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleOrderEvents(
            @Payload List<OrderKafkaEvent> events,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        for (OrderKafkaEvent event : events) {
            if (event.getItems() != null) {
                for (var item : event.getItems()) {
                    rankingEventService.handleOrder(
                        item.getProductId(),
                        item.getPrice(),
                        item.getQuantity()
                    );
                }
            }
        }
    }
}
