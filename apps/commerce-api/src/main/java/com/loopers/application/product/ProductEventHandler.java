package com.loopers.application.product;

import com.loopers.domain.product.ProductEvent;
import com.loopers.config.kafka.KafkaEventPublisher;
import com.loopers.event.product.ProductViewKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventHandler {
    
    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductViewed(ProductEvent.Viewed event) {
        try {
            // Kafka로 상품 조회 이벤트 발행
            ProductViewKafkaEvent kafkaEvent = ProductViewKafkaEvent.productViewed(event.productId());
            kafkaEventPublisher.publishEventAsync("product-view-events", event.productId().toString(), kafkaEvent);
            
            log.info("상품 조회 이벤트 발행 완료 - productId: {}", event.productId());
        } catch (Exception e) {
            log.error("상품 조회 이벤트 발행 중 오류 발생 - productId: {}, error: {}", 
                    event.productId(), e.getMessage());
        }
    }
}
