package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandService brandService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long brandId;

    @BeforeEach
    void setUp() {
        // 테스트용 브랜드 생성
        brandId = brandService.create("나이키").getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 목록 조회시 브랜드 정보가 포함된 목록이 반환된다")
    @Test
    void 상품목록_조회시_브랜드정보가_포함된_목록이_반환된다() {
        // given
        ProductCommand.Create productCommand = new ProductCommand.Create(
                "나이키 티셔츠", brandId, 15000L, 10L, 0L
        );
        productFacade.createProduct(productCommand);

        ProductCriteria criteria = ProductCriteria.orderByCreatedAt(false);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductInfo> result = productFacade.findProducts(criteria, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        ProductInfo productInfo = result.getContent().get(0);
        assertThat(productInfo.name()).isEqualTo("나이키 티셔츠");
        assertThat(productInfo.brandId()).isEqualTo(brandId);
        assertThat(productInfo.brandName()).isEqualTo("나이키");
        assertThat(productInfo.price()).isEqualTo(15000L);
    }
    
    @DisplayName("상품 상세 조회시 브랜드 정보가 포함된 상품 정보가 반환된다")
    @Test
    void 상품상세_조회시_브랜드정보가_포함된_상품정보가_반환된다() {
        // given
        ProductCommand.Create productCommand = new ProductCommand.Create(
                "나이키 티셔츠", brandId, 15000L, 10L, 0L
        );
        ProductInfo createdProduct = productFacade.createProduct(productCommand);

        // when
        ProductInfo result = productFacade.findProductById(createdProduct.id());

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("나이키 티셔츠");
        assertThat(result.brandId()).isEqualTo(brandId);
        assertThat(result.brandName()).isEqualTo("나이키");
        assertThat(result.price()).isEqualTo(15000L);
    }
    
    @DisplayName("존재하지 않는 브랜드로 상품을 생성하면 실패한다")
    @Test
    void 존재하지않는_브랜드로_상품생성시_실패한다() {
        // given
        Long nonExistentBrandId = 999L;
        ProductCommand.Create productCommand = new ProductCommand.Create(
                "테스트 상품", nonExistentBrandId, 15000L, 10L, 0L
        );

        // when & then
        assertThatThrownBy(() -> {
            productFacade.createProduct(productCommand);
        }).isInstanceOf(CoreException.class);
    }
    
    @DisplayName("존재하지 않는 상품을 조회하면 실패한다")
    @Test
    void 존재하지않는_상품_조회시_실패한다() {
        // given
        Long nonExistentProductId = 999L;

        // when & then
        assertThatThrownBy(() -> {
            productFacade.findProductById(nonExistentProductId);
        }).isInstanceOf(CoreException.class);
    }
}
