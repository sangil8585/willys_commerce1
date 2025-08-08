package com.loopers.domain.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandService brandService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @BeforeEach
    void setUp() {
        createTestProducts();
    }

    private void createTestProducts() {
        // 먼저 필요한 브랜드들을 생성
        BrandEntity brand1 = brandService.create("메종키츠네");
        BrandEntity brand2 = brandService.create("APC");
        BrandEntity brand3 = brandService.create("지오다노");
        BrandEntity brand4 = brandService.create("코닥");
        BrandEntity brand5 = brandService.create("폴로");

        ProductCommand.Create product1 = new ProductCommand.Create("메종키츠네 티셔츠", brand1.getId(), 15000L, 10L, 1L);
        ProductCommand.Create product1_2 = new ProductCommand.Create("메종키츠네 후드", brand1.getId(), 25000L, 5L, 2L);
        ProductCommand.Create product2 = new ProductCommand.Create("APC 티셔츠", brand2.getId(), 25000L, 5L, 4L);
        ProductCommand.Create product3 = new ProductCommand.Create("지오다노 티셔츠", brand3.getId(), 3000L, 20L, 3L);
        ProductCommand.Create product4 = new ProductCommand.Create("코닥 티셔츠", brand4.getId(), 1200L, 8L, 5L);
        ProductCommand.Create product5 = new ProductCommand.Create("폴로 티셔츠", brand5.getId(), 20000L, 15L, 10L);

        productFacade.createProduct(product1);
        productFacade.createProduct(product1_2);
        productFacade.createProduct(product2);
        productFacade.createProduct(product3);
        productFacade.createProduct(product4);
        productFacade.createProduct(product5);
    }

    @DisplayName("상품 생성")
    @Nested
    class CreateProduct {

        @DisplayName("유효한 상품 정보로 상품을 생성할 수 있다")
        @Test
        void 유효한_상품정보로_상품_생성() {
            // given
            BrandEntity brand = brandService.create("나이키");
            ProductCommand.Create command = new ProductCommand.Create(
                    "새로운 테스트 상품", brand.getId(), 50000L, 25L, 0L
            );

            // when
            ProductInfo result = productFacade.createProduct(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(command.name());
            assertThat(brand.getId()).isEqualTo(command.brandId());
            assertThat(result.price()).isEqualTo(command.price());
            assertThat(result.stock()).isEqualTo(command.stock());
            assertThat(result.likes()).isEqualTo(0L); // 새로 생성된 상품은 좋아요 0
        }

        @DisplayName("상품명이 null이면 예외가 발생한다")
        @Test
        void 상품명_null_예외() {
            // given
            ProductCommand.Create command = new ProductCommand.Create(
                    null, 1L, 50000L, 25L, 0L
            );

            // when & then
            assertThatThrownBy(() -> productFacade.createProduct(command))
                    .isInstanceOf(CoreException.class);
        }

        @DisplayName("브랜드 ID가 null이면 예외가 발생한다")
        @Test
        void 브랜드ID_null_예외() {
            // given
            ProductCommand.Create command = new ProductCommand.Create(
                    "테스트 상품", null, 50000L, 25L, 0L
            );

            // when & then
            assertThatThrownBy(() -> productFacade.createProduct(command))
                    .isInstanceOf(CoreException.class);
        }

        @DisplayName("가격이 null이면 예외가 발생한다")
        @Test
        void 가격_null_예외() {
            // given
            ProductCommand.Create command = new ProductCommand.Create(
                    "테스트 상품", 1L, null, 25L, 0L
            );

            // when & then
            assertThatThrownBy(() -> productFacade.createProduct(command))
                    .isInstanceOf(CoreException.class);
        }

        @DisplayName("재고가 null이면 예외가 발생한다")
        @Test
        void 재고_null_예외() {
            // given
            ProductCommand.Create command = new ProductCommand.Create(
                    "테스트 상품", 1L, 50000L, null, 0L
            );

            // when & then
            assertThatThrownBy(() -> productFacade.createProduct(command))
                    .isInstanceOf(CoreException.class);
        }
    }

    @DisplayName("상품 목록 조회")
    @Nested
    class FindProducts {

        @DisplayName("기본 페이징 조회 - 최신순 정렬")
        @Test
        void 기본_페이징_조회_최신순() {
            // given
            ProductCriteria criteria = ProductCriteria.orderByCreatedAt(false);
            Pageable pageable = PageRequest.of(0, 3);

            // when
            Page<ProductInfo> result = productFacade.findProducts(criteria, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.hasNext()).isTrue();
        }

        @DisplayName("가격 낮은순 정렬 조회")
        @Test
        void 가격_낮은순_정렬_조회() {
            // given
            ProductCriteria criteria = ProductCriteria.orderByPrice(true);
            Pageable pageable = PageRequest.of(0, 6);

            // when
            Page<ProductInfo> result = productFacade.findProducts(criteria, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(6);

            // 가격이 오름차순으로 정렬되었는지 확인
            List<ProductInfo> products = result.getContent();
            assertThat(products.get(0).price()).isLessThanOrEqualTo(products.get(1).price());
            assertThat(products.get(1).price()).isLessThanOrEqualTo(products.get(2).price());
        }

        @DisplayName("가격 높은순 정렬 조회")
        @Test
        void 가격_높은순_정렬_조회() {
            // given
            ProductCriteria criteria = ProductCriteria.orderByPrice(false);
            Pageable pageable = PageRequest.of(0, 6);

            // when
            Page<ProductInfo> result = productFacade.findProducts(criteria, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(6);

            // 가격이 내림차순으로 정렬되었는지 확인
            List<ProductInfo> products = result.getContent();
            assertThat(products.get(0).price()).isGreaterThanOrEqualTo(products.get(1).price());
            assertThat(products.get(1).price()).isGreaterThanOrEqualTo(products.get(2).price());
        }

        @DisplayName("브랜드별 조회")
        @Test
        void 브랜드별_조회() {
            // given
            ProductCriteria criteria = ProductCriteria.brandIdEquals(1L);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<ProductInfo> result = productFacade.findProducts(criteria, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(product -> product.brandId().equals(1L));
        }

        @DisplayName("재고 있는 상품만 조회")
        @Test
        void 재고_있는_상품만_조회() {
            // given
            ProductCriteria criteria = ProductCriteria.stockGreaterThan(10L);
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<ProductInfo> result = productFacade.findProducts(criteria, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(product -> product.stock() > 10L);
        }

        @DisplayName("페이징 처리 테스트")
        @Test
        void 페이징_처리_테스트() {
            // given
            ProductCriteria criteria = ProductCriteria.orderByCreatedAt(false);
            Pageable firstPage = PageRequest.of(0, 2);
            Pageable secondPage = PageRequest.of(1, 2);

            // when
            Page<ProductInfo> firstPageResult = productFacade.findProducts(criteria, firstPage);
            Page<ProductInfo> secondPageResult = productFacade.findProducts(criteria, secondPage);

            // then
            assertThat(firstPageResult.getContent()).hasSize(2);
            assertThat(firstPageResult.getTotalElements()).isEqualTo(6);
            assertThat(firstPageResult.getTotalPages()).isEqualTo(3);
            assertThat(firstPageResult.isFirst()).isTrue();
            assertThat(firstPageResult.isLast()).isFalse();

            assertThat(secondPageResult.getContent()).hasSize(2);
            assertThat(secondPageResult.isFirst()).isFalse();
            assertThat(secondPageResult.isLast()).isFalse();

            // 첫 번째 페이지와 두 번째 페이지의 상품이 다른지 확인
            assertThat(firstPageResult.getContent()).doesNotContainAnyElementsOf(secondPageResult.getContent());
        }
    }
}
