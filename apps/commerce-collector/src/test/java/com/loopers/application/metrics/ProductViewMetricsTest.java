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
public class ProductViewMetricsTest {

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

    @DisplayName("상품 조회 이벤트 처리 시 조회수가 증가한다")
    @Test
    void 상품조회_이벤트처리시_조회수가_증가한다() {
        // given
        ProductViewKafkaEvent event = ProductViewKafkaEvent.productViewed(productId);
        MetricsCriteria criteria = MetricsCriteria.of(event, "product-view-events", 0, 1L);

        // when
        MetricsResult result = metricsFacade.processProductViewMetrics(criteria);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProcessedCount()).isEqualTo(1);

        Optional<ProductMetricsEntity> metrics = productMetricsRepository
                .findByProductIdAndDate(productId, testDate);
        
        assertThat(metrics).isPresent();
        assertThat(metrics.get().getViewCount()).isEqualTo(1);
        assertThat(metrics.get().getLikeCount()).isEqualTo(0);
        assertThat(metrics.get().getOrderCount()).isEqualTo(0);
        assertThat(metrics.get().getOrderQuantity()).isEqualTo(0);
    }

    @DisplayName("같은 상품을 여러 번 조회하면 조회수가 누적된다")
    @Test
    void 같은상품을_여러번_조회하면_조회수가_누적된다() {
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

    @DisplayName("ProductViewKafkaEvent 생성 테스트")
    @Test
    void ProductViewKafkaEvent_생성_테스트() {
        // given
        Long testProductId = 456L;

        // when
        ProductViewKafkaEvent event = ProductViewKafkaEvent.productViewed(testProductId);

        // then
        assertThat(event).isNotNull();
        assertThat(event.getProductId()).isEqualTo(testProductId);
        assertThat(event.getEventType()).isEqualTo("ProductViewed");
        assertThat(event.getSource()).isEqualTo("commerce-api");
        assertThat(event.getVersion()).isEqualTo("1.0");
    }
}
