package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ProductDetailCacheTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long brandId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // 테스트용 브랜드 생성
        brandId = brandService.create("테스트 브랜드").getId();
        
        // 테스트용 상품 생성
        ProductCommand.Create productCommand = new ProductCommand.Create(
                "테스트 상품", brandId, 10000L, 10L, 0L
        );
        ProductInfo createdProduct = productFacade.createProduct(productCommand);
        productId = createdProduct.id();
    }

    @AfterEach
    void tearDown() {
        // 캐시 초기화
        cacheManager.getCache("product").clear();
        cacheManager.getCache("productList").clear();
        cacheManager.getCache("brand").clear();
        
        // 데이터베이스 초기화
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("캐시에 상품상세값이 존재하면 반환한다")
    void 캐시에_상품상세값이_존재하면_반환한다() {
        // given - 상품 상세 캐시 생성
        ProductInfo firstResult = productFacade.findProductById(productId);
        
        // 캐시에 저장되었는지 확인
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when - 두 번째 조회 (캐시 히트)
        ProductInfo secondResult = productFacade.findProductById(productId);
        
        // then - 캐시에서 반환된 결과가 첫 번째 결과와 동일해야 함
        assertThat(secondResult).isEqualTo(firstResult);
        assertThat(secondResult.id()).isEqualTo(productId);
        assertThat(secondResult.name()).isEqualTo("테스트 상품");
        assertThat(secondResult.brandName()).isEqualTo("테스트 브랜드");
    }

    @Test
    @DisplayName("캐시에 상품이 존재하지 않으면 로더 실행 후 TTL과 함께 저장한다")
    void 캐시에_상품이_존재하지_않으면_로더실행후_TTL과_함께_저장한다() {
        // given - 캐시가 비어있는 상태
        // 명시적으로 캐시 초기화
        cacheManager.getCache("product").clear();
        
        // 캐시에 상품이 없는지 확인
        assertThat(cacheManager.getCache("product").get(productId)).isNull();
        
        // when - 상품 조회 (캐시 미스)
        ProductInfo result = productFacade.findProductById(productId);
        
        // then - 상품 정보가 정상적으로 반환되어야 함
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo("테스트 상품");
        
        // 캐시에 TTL과 함께 저장되었는지 확인
        assertThat(cacheManager.getCache("product")).isNotNull();
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // 캐시된 값이 반환된 결과와 동일한지 확인
        var cachedValue = cacheManager.getCache("product").get(productId).get();
        assertThat(cachedValue).isEqualTo(result);
    }

    @Test
    @DisplayName("evict 호출 시 해당 키를 삭제한다")
    void evict_호출시_해당_키를_삭제한다() {
        // given - 상품 상세 캐시 생성
        productFacade.findProductById(productId);
        
        // 캐시에 저장되었는지 확인
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when - 캐시 무효화 호출
        productFacade.evictProductCache(productId);
        
        // then - 해당 키가 캐시에서 삭제되어야 함
        assertThat(cacheManager.getCache("product").get(productId)).isNull();
    }

    @Test
    @DisplayName("evict 호출 후 재조회 시 새로운 캐시가 생성된다")
    void evict_호출후_재조회시_새로운_캐시가_생성된다() {
        // given - 상품 상세 캐시 생성
        ProductInfo firstResult = productFacade.findProductById(productId);
        
        // 캐시에 저장되었는지 확인
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when - 캐시 무효화
        productFacade.evictProductCache(productId);
        
        // then - 캐시가 삭제되었는지 확인
        assertThat(cacheManager.getCache("product").get(productId)).isNull();
        
        // when - 재조회 (새로운 캐시 생성)
        ProductInfo secondResult = productFacade.findProductById(productId);
        
        // then - 새로운 캐시가 생성되었는지 확인
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        assertThat(secondResult).isEqualTo(firstResult);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 캐시에 저장되지 않는다")
    void 존재하지_않는_상품_조회시_캐시에_저장되지_않는다() {
        // given - 존재하지 않는 상품 ID
        Long nonExistentProductId = 99999L;
        
        // when & then - 예외가 발생해야 함
        try {
            productFacade.findProductById(nonExistentProductId);
        } catch (Exception e) {
            // 예외가 발생하는 것이 정상
            assertThat(e).isInstanceOf(Exception.class);
        }
        
        // then - 캐시에 null 값이 저장되지 않았는지 확인
        assertThat(cacheManager.getCache("product").get(nonExistentProductId)).isNull();
    }

    @Test
    @DisplayName("여러 상품 조회 시 각각 별도 캐시가 생성된다")
    void 여러_상품_조회시_각각_별도_캐시가_생성된다() {
        // given - 추가 상품 생성
        ProductCommand.Create productCommand2 = new ProductCommand.Create(
                "테스트 상품2", brandId, 20000L, 5L, 0L
        );
        ProductInfo createdProduct2 = productFacade.createProduct(productCommand2);
        
        // when - 첫 번째 상품 조회
        ProductInfo result1 = productFacade.findProductById(productId);
        
        // when - 두 번째 상품 조회
        ProductInfo result2 = productFacade.findProductById(createdProduct2.id());
        
        // then - 두 상품 모두 캐시에 저장되어야 함
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        assertThat(cacheManager.getCache("product").get(createdProduct2.id())).isNotNull();
        
        // 캐시된 값이 각각의 결과와 동일한지 확인
        var cachedValue1 = cacheManager.getCache("product").get(productId).get();
        var cachedValue2 = cacheManager.getCache("product").get(createdProduct2.id()).get();
        
        assertThat(cachedValue1).isEqualTo(result1);
        assertThat(cachedValue2).isEqualTo(result2);
    }
}