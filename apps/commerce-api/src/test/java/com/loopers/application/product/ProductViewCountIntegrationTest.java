package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductEvent;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RecordApplicationEvents
public class ProductViewCountIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long brandId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // 테스트용 브랜드 생성
        brandId = brandService.create("나이키").getId();
        
        // 테스트용 상품 생성
        ProductCommand.Create productCommand = new ProductCommand.Create(
                "나이키 티셔츠", brandId, 15000L, 10L, 0L
        );
        ProductInfo createdProduct = productFacade.createProduct(productCommand);
        productId = createdProduct.id();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 조회 시 ProductEvent.Viewed 이벤트가 발행된다")
    @Test
    void 상품조회시_ProductEvent_Viewed_이벤트가_발행된다() {
        // when
        ProductInfo result = productFacade.findProductById(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("나이키 티셔츠");
        
        // 이벤트 발행 확인
        assertThat(applicationEvents.stream(ProductEvent.Viewed.class))
                .hasSize(1);
        
        ProductEvent.Viewed publishedEvent = applicationEvents.stream(ProductEvent.Viewed.class)
                .findFirst()
                .orElseThrow();
        
        assertThat(publishedEvent.productId()).isEqualTo(productId);
    }

    @DisplayName("상품을 여러 번 조회하면 이벤트가 여러 번 발행된다")
    @Test
    void 상품을_여러번_조회하면_이벤트가_여러번_발행된다() {
        // when
        productFacade.findProductById(productId);
        productFacade.findProductById(productId);
        productFacade.findProductById(productId);

        // then
        assertThat(applicationEvents.stream(ProductEvent.Viewed.class))
                .hasSize(3);
        
        // 모든 이벤트가 같은 상품 ID를 가지고 있는지 확인
        applicationEvents.stream(ProductEvent.Viewed.class)
                .forEach(event -> assertThat(event.productId()).isEqualTo(productId));
    }

    @DisplayName("존재하지 않는 상품을 조회하면 이벤트가 발행되지 않는다")
    @Test
    void 존재하지않는_상품_조회시_이벤트가_발행되지_않는다() {
        // given
        Long nonExistentProductId = 999L;

        // when & then
        try {
            productFacade.findProductById(nonExistentProductId);
        } catch (Exception e) {
            // 예외 발생 예상
        }
        
        // 이벤트가 발행되지 않았는지 확인
        assertThat(applicationEvents.stream(ProductEvent.Viewed.class))
                .isEmpty();
    }

    @DisplayName("상품 목록 조회 시에는 조회수 이벤트가 발행되지 않는다")
    @Test
    void 상품목록_조회시에는_조회수_이벤트가_발행되지_않는다() {
        // when
        productFacade.findProducts(ProductCriteria.orderByCreatedAt(false), 
                PageRequest.of(0, 10));

        // then
        assertThat(applicationEvents.stream(ProductEvent.Viewed.class))
                .isEmpty();
    }

    @DisplayName("ProductEvent.Viewed 이벤트 생성 테스트")
    @Test
    void ProductEvent_Viewed_이벤트_생성_테스트() {
        // given
        Long testProductId = 456L;

        // when
        ProductEvent.Viewed event = ProductEvent.Viewed.of(testProductId);

        // then
        assertThat(event).isNotNull();
        assertThat(event.productId()).isEqualTo(testProductId);
    }
}
