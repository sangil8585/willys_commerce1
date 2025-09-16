package com.loopers.application.metrics;

import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.event.order.OrderKafkaEvent;
import com.loopers.event.like.LikeKafkaEvent;
import com.loopers.event.product.ProductViewKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsFacade {

    private final ProductMetricsService productMetricsService;

    @Transactional
    public MetricsResult processOrderMetrics(MetricsCriteria criteria) {
        try {
            OrderKafkaEvent event = (OrderKafkaEvent) criteria.getEvent();
            
            log.info("주문 메트릭 처리 시작 - eventId: {}, eventType: {}, orderId: {}, topic: {}, partition: {}, offset: {}",
                    event.getEventId(), event.getEventType(), event.getOrderId(), 
                    criteria.getTopic(), criteria.getPartition(), criteria.getOffset());

            int processedCount = 0;

            switch (event.getEventType()) {
                case "OrderCompleted" -> processedCount = handleOrderCompleted(event, criteria.getDate());
                case "PaymentCompleted" -> processedCount = handlePaymentCompleted(event, criteria.getDate());
                default -> log.warn("알 수 없는 주문 이벤트 타입: {}", event.getEventType());
            }

            log.info("주문 메트릭 처리 완료 - eventId: {}, eventType: {}, processedCount: {}",
                    event.getEventId(), event.getEventType(), processedCount);

            return MetricsResult.success(
                    event.getEventId(),
                    event.getEventType(),
                    "주문 메트릭 처리 완료",
                    processedCount
            );

        } catch (Exception e) {
            log.error("주문 메트릭 처리 실패 - eventId: {}, eventType: {}, error: {}",
                    criteria.getEvent().getEventId(), criteria.getEvent().getEventType(), e.getMessage(), e);

            return MetricsResult.failure(
                    criteria.getEvent().getEventId(),
                    criteria.getEvent().getEventType(),
                    e.getMessage()
            );
        }
    }

    @Transactional
    public MetricsResult processLikeMetrics(MetricsCriteria criteria) {
        try {
            LikeKafkaEvent event = (LikeKafkaEvent) criteria.getEvent();
            
            log.info("좋아요 메트릭 처리 시작 - eventId: {}, eventType: {}, userId: {}, productId: {}, topic: {}, partition: {}, offset: {}",
                    event.getEventId(), event.getEventType(), event.getUserId(), event.getProductId(),
                    criteria.getTopic(), criteria.getPartition(), criteria.getOffset());

            int processedCount = 0;

            switch (event.getEventType()) {
                case "LikeCreated" -> processedCount = handleLikeCreated(event, criteria.getDate());
                case "LikeRemoved" -> processedCount = handleLikeRemoved(event, criteria.getDate());
                default -> log.warn("알 수 없는 좋아요 이벤트 타입: {}", event.getEventType());
            }

            log.info("좋아요 메트릭 처리 완료 - eventId: {}, eventType: {}, processedCount: {}",
                    event.getEventId(), event.getEventType(), processedCount);

            return MetricsResult.success(
                    event.getEventId(),
                    event.getEventType(),
                    "좋아요 메트릭 처리 완료",
                    processedCount
            );

        } catch (Exception e) {
            log.error("좋아요 메트릭 처리 실패 - eventId: {}, eventType: {}, error: {}",
                    criteria.getEvent().getEventId(), criteria.getEvent().getEventType(), e.getMessage(), e);

            return MetricsResult.failure(
                    criteria.getEvent().getEventId(),
                    criteria.getEvent().getEventType(),
                    e.getMessage()
            );
        }
    }

    private int handleOrderCompleted(OrderKafkaEvent event, LocalDate date) {
        int processedCount = 0;
        
        // 각 상품별로 주문 수량 집계
        for (OrderKafkaEvent.OrderItem item : event.getItems()) {
            productMetricsService.upsertProductMetrics(
                item.getProductId(), date, "order", 1);

            productMetricsService.upsertProductMetrics(
                item.getProductId(), date, "order_quantity", item.getQuantity());
            
            processedCount += 2; // 주문 + 퀀티티
        }
        
        return processedCount;
    }

    private int handlePaymentCompleted(OrderKafkaEvent event, LocalDate date) {
        return 0;
    }

    private int handleLikeCreated(LikeKafkaEvent event, LocalDate date) {
        productMetricsService.upsertProductMetrics(
            event.getProductId(), date, "like", 1);
        
        return 1;
    }

    private int handleLikeRemoved(LikeKafkaEvent event, LocalDate date) {
        productMetricsService.upsertProductMetrics(
            event.getProductId(), date, "like", -1);
        
        return 1;
    }

    @Transactional
    public MetricsResult processProductViewMetrics(MetricsCriteria criteria) {
        try {
            ProductViewKafkaEvent event = (ProductViewKafkaEvent) criteria.getEvent();
            
            log.info("상품 조회 메트릭 처리 시작 - eventId: {}, eventType: {}, productId: {}, topic: {}, partition: {}, offset: {}",
                    event.getEventId(), event.getEventType(), event.getProductId(),
                    criteria.getTopic(), criteria.getPartition(), criteria.getOffset());

            int processedCount = 0;

            switch (event.getEventType()) {
                case "ProductViewed" -> processedCount = handleProductViewed(event, criteria.getDate());
                default -> log.warn("알 수 없는 상품 조회 이벤트 타입: {}", event.getEventType());
            }

            log.info("상품 조회 메트릭 처리 완료 - eventId: {}, eventType: {}, processedCount: {}",
                    event.getEventId(), event.getEventType(), processedCount);

            return MetricsResult.success(
                    event.getEventId(),
                    event.getEventType(),
                    "상품 조회 메트릭 처리 완료",
                    processedCount
            );

        } catch (Exception e) {
            log.error("상품 조회 메트릭 처리 실패 - eventId: {}, eventType: {}, error: {}",
                    criteria.getEvent().getEventId(), criteria.getEvent().getEventType(), e.getMessage(), e);

            return MetricsResult.failure(
                    criteria.getEvent().getEventId(),
                    criteria.getEvent().getEventType(),
                    e.getMessage()
            );
        }
    }

    private int handleProductViewed(ProductViewKafkaEvent event, LocalDate date) {
        productMetricsService.upsertProductMetrics(
            event.getProductId(), date, "view", 1);
        
        return 1;
    }
}
