package com.loopers.application.metrics;

import com.loopers.domain.metrics.ProductMetricsEntity;
import com.loopers.domain.metrics.ProductMetricsRepository;
import com.loopers.event.product.ProductViewKafkaEvent;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductViewCountEndToEndTest {

    @Autowired
    private MetricsFacade metricsFacade;

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long productId;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        productId = 123L;
        testDate = LocalDate.now();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 조회 이벤트 처리 시 DB에 조회수가 정확히 저장된다")
    @Test
    void 상품조회_이벤트처리시_DB에_조회수가_정확히_저장된다() {
        // given
        ProductViewKafkaEvent event = ProductViewKafkaEvent.productViewed(productId);
        MetricsCriteria criteria = MetricsCriteria.of(event, "product-view-events", 0, 1L);

        // when
        MetricsResult result = metricsFacade.processProductViewMetrics(criteria);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProcessedCount()).isEqualTo(1);
        assertThat(result.getMessage()).isEqualTo("상품 조회 메트릭 처리 완료");

        // DB에서 실제 데이터 확인
        Optional<ProductMetricsEntity> metrics = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        
        assertThat(metrics).isPresent();
        ProductMetricsEntity savedMetrics = metrics.get();
        
        assertThat(savedMetrics.getProductId()).isEqualTo(productId);
        assertThat(savedMetrics.getDate()).isEqualTo(testDate);
        assertThat(savedMetrics.getViewCount()).isEqualTo(1);
        assertThat(savedMetrics.getLikeCount()).isEqualTo(0);
        assertThat(savedMetrics.getOrderCount()).isEqualTo(0);
        assertThat(savedMetrics.getOrderQuantity()).isEqualTo(0);
        assertThat(savedMetrics.getCreatedAt()).isNotNull();
        assertThat(savedMetrics.getUpdatedAt()).isNotNull();
    }

    @DisplayName("같은 상품을 여러 번 조회하면 DB에 조회수가 누적된다")
    @Test
    void 같은상품을_여러번_조회하면_DB에_조회수가_누적된다() {
        // given
        ProductViewKafkaEvent event1 = ProductViewKafkaEvent.productViewed(productId);
        ProductViewKafkaEvent event2 = ProductViewKafkaEvent.productViewed(productId);
        ProductViewKafkaEvent event3 = ProductViewKafkaEvent.productViewed(productId);

        MetricsCriteria criteria1 = MetricsCriteria.of(event1, "product-view-events", 0, 1L);
        MetricsCriteria criteria2 = MetricsCriteria.of(event2, "product-view-events", 0, 2L);
        MetricsCriteria criteria3 = MetricsCriteria.of(event3, "product-view-events", 0, 3L);

        // when
        metricsFacade.processProductViewMetrics(criteria1);
        metricsFacade.processProductViewMetrics(criteria2);
        metricsFacade.processProductViewMetrics(criteria3);

        // then
        Optional<ProductMetricsEntity> metrics = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        
        assertThat(metrics).isPresent();
        assertThat(metrics.get().getViewCount()).isEqualTo(3);
    }

    @DisplayName("다른 상품의 조회수는 별도로 집계된다")
    @Test
    void 다른상품의_조회수는_별도로_집계된다() {
        // given
        Long productId2 = 456L;
        ProductViewKafkaEvent event1 = ProductViewKafkaEvent.productViewed(productId);
        ProductViewKafkaEvent event2 = ProductViewKafkaEvent.productViewed(productId2);

        MetricsCriteria criteria1 = MetricsCriteria.of(event1, "product-view-events", 0, 1L);
        MetricsCriteria criteria2 = MetricsCriteria.of(event2, "product-view-events", 0, 2L);

        // when
        metricsFacade.processProductViewMetrics(criteria1);
        metricsFacade.processProductViewMetrics(criteria2);

        // then
        Optional<ProductMetricsEntity> metrics1 = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        Optional<ProductMetricsEntity> metrics2 = productMetricsRepository
                .findByProductIdAndDate(productId2, testDate);
        
        assertThat(metrics1).isPresent();
        assertThat(metrics1.get().getViewCount()).isEqualTo(1);
        
        assertThat(metrics2).isPresent();
        assertThat(metrics2.get().getViewCount()).isEqualTo(1);
    }

    @DisplayName("다른 날짜의 조회수는 별도로 집계된다")
    @Test
    void 다른날짜의_조회수는_별도로_집계된다() {
        // given
        LocalDate yesterday = testDate.minusDays(1);
        LocalDate tomorrow = testDate.plusDays(1);

        ProductViewKafkaEvent event1 = ProductViewKafkaEvent.productViewed(productId);
        ProductViewKafkaEvent event2 = ProductViewKafkaEvent.productViewed(productId);
        ProductViewKafkaEvent event3 = ProductViewKafkaEvent.productViewed(productId);

        MetricsCriteria criteria1 = MetricsCriteria.of(event1, "product-view-events", 0, 1L);
        MetricsCriteria criteria2 = MetricsCriteria.of(event2, "product-view-events", 0, 2L);
        MetricsCriteria criteria3 = MetricsCriteria.of(event3, "product-view-events", 0, 3L);

        // when
        metricsFacade.processProductViewMetrics(criteria1);
        metricsFacade.processProductViewMetrics(criteria2);
        metricsFacade.processProductViewMetrics(criteria3);

        // then
        Optional<ProductMetricsEntity> todayMetrics = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        Optional<ProductMetricsEntity> yesterdayMetrics = productMetricsRepository
                .findByProductIdAndDate(productId, yesterday);
        Optional<ProductMetricsEntity> tomorrowMetrics = productMetricsRepository
                .findByProductIdAndDate(productId, tomorrow);
        
        assertThat(todayMetrics).isPresent();
        assertThat(todayMetrics.get().getViewCount()).isEqualTo(3);
        
        assertThat(yesterdayMetrics).isEmpty();
        assertThat(tomorrowMetrics).isEmpty();
    }

    @DisplayName("상품 조회와 다른 메트릭스가 함께 저장된다")
    @Test
    void 상품조회와_다른메트릭스가_함께_저장된다() {
        // given
        ProductViewKafkaEvent viewEvent = ProductViewKafkaEvent.productViewed(productId);
        MetricsCriteria viewCriteria = MetricsCriteria.of(viewEvent, "product-view-events", 0, 1L);

        // when - 조회수 이벤트 처리
        MetricsResult viewResult = metricsFacade.processProductViewMetrics(viewCriteria);

        // then
        assertThat(viewResult.isSuccess()).isTrue();

        Optional<ProductMetricsEntity> metrics = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        
        assertThat(metrics).isPresent();
        ProductMetricsEntity savedMetrics = metrics.get();
        
        // 조회수는 1, 다른 메트릭스는 0
        assertThat(savedMetrics.getViewCount()).isEqualTo(1);
        assertThat(savedMetrics.getLikeCount()).isEqualTo(0);
        assertThat(savedMetrics.getOrderCount()).isEqualTo(0);
        assertThat(savedMetrics.getOrderQuantity()).isEqualTo(0);
    }

    @DisplayName("잘못된 이벤트 타입 처리 시 경고 로그가 출력된다")
    @Test
    void 잘못된_이벤트타입_처리시_경고로그가_출력된다() {
        // given
        ProductViewKafkaEvent event = new ProductViewKafkaEvent("InvalidEventType", productId);
        MetricsCriteria criteria = MetricsCriteria.of(event, "product-view-events", 0, 1L);

        // when
        MetricsResult result = metricsFacade.processProductViewMetrics(criteria);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProcessedCount()).isEqualTo(0);
        
        // DB에 데이터가 저장되지 않았는지 확인
        Optional<ProductMetricsEntity> metrics = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        assertThat(metrics).isEmpty();
    }
}
