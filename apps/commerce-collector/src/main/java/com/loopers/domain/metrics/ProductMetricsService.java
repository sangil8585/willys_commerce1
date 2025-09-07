package com.loopers.domain.metrics;

import com.loopers.domain.metrics.ProductMetricsEntity;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMetricsService {

    private final ProductMetricsRepository metricsRepository;

    @Transactional
    public void upsertProductMetrics(Long productId, LocalDate date, String metricType, int value) {
        try {
            Optional<ProductMetricsEntity> existing = metricsRepository
                    .findByProductIdAndDate(productId, date);

            if (existing.isPresent()) {
                ProductMetricsEntity metrics = existing.get();
                updateMetrics(metrics, metricType, value);
                metricsRepository.save(metrics);
                
                log.info("상품 메트릭 업데이트 - productId: {}, date: {}, type: {}, value: {}", 
                        productId, date, metricType, value);
            } else {
                ProductMetricsEntity metrics = createNewMetrics(productId, date, metricType, value);
                metricsRepository.save(metrics);
                
                log.info("상품 메트릭 생성 - productId: {}, date: {}, type: {}, value: {}", 
                        productId, date, metricType, value);
            }

        } catch (Exception e) {
            log.error("상품 메트릭 처리 실패 - productId: {}, date: {}, type: {}, value: {}, error: {}", 
                    productId, date, metricType, value, e.getMessage(), e);
        }
    }

    private void updateMetrics(ProductMetricsEntity metrics, String metricType, int value) {
        switch (metricType) {
            case "like" -> metrics.addLikeCount(value);
            case "order" -> metrics.addOrderCount(value);
            case "order_quantity" -> metrics.addOrderQuantity(value);
            case "view" -> metrics.addViewCount(value);
            default -> log.warn("알 수 없는 메트릭 타입: {}", metricType);
        }
    }

    private ProductMetricsEntity createNewMetrics(Long productId, LocalDate date, String metricType, int value) {
        return ProductMetricsEntity.builder()
                .productId(productId)
                .date(date)
                .likeCount(metricType.equals("like") ? value : 0)
                .orderCount(metricType.equals("order") ? value : 0)
                .orderQuantity(metricType.equals("order_quantity") ? value : 0)
                .viewCount(metricType.equals("view") ? value : 0)
                .build();
    }
}
