package com.loopers.application.like;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
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
public class LikeCacheIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandService brandService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long brandId;
    private Long productId;
    private Long userId;

    @BeforeEach
    void setUp() {
        brandId = brandService.create("테스트 브랜드").getId();
        
        ProductCommand.Create productCommand = new ProductCommand.Create(
                "테스트 상품", brandId, 10000L, 10L, 0L
        );
        ProductInfo createdProduct = productFacade.createProduct(productCommand);
        productId = createdProduct.id();
        
        UserCommand.Create userCommand = UserCommand.Create.of("testUser", "MALE", "1990-01-01", "test@example.com");
        UserEntity user = userService.signUp(userCommand);
        userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("like가 새로 생성되면 커밋 후 bump 호출한다")
    void like가_새로_생성되면_커밋후_bump_호출한다() {
        // given
        // 상품 상세 캐시 생성
        productFacade.findProductById(productId);
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when - 좋아요 등록
        LikeCommand.Create createCommand = new LikeCommand.Create(userId, productId);
        likeFacade.like(createCommand);
        
        // then - 상품 상세 캐시가 무효화되어야 함 (bump 호출)
        assertThat(cacheManager.getCache("product").get(productId)).isNull();
        
        // 좋아요가 실제로 생성되었는지 확인
        assertThat(likeService.existsByUserIdAndProductId(userId, productId)).isTrue();
    }

    @Test
    @DisplayName("좋아요취소가 실제로 삭제되면 커밋 후 bump 호출")
    void 좋아요취소가_실제로_삭제되면_커밋후_bump_호출() {
        // given - 좋아요 등록
        LikeCommand.Create createCommand = new LikeCommand.Create(userId, productId);
        likeFacade.like(createCommand);
        
        // 상품 상세 캐시 생성
        productFacade.findProductById(productId);
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when - 좋아요 취소
        likeFacade.unlike(userId, productId);
        
        // then - 상품 상세 캐시가 무효화되어야 함 (bump 호출)
        assertThat(cacheManager.getCache("product").get(productId)).isNull();
        
        // 좋아요가 실제로 삭제되었는지 확인
        assertThat(likeService.existsByUserIdAndProductId(userId, productId)).isFalse();
    }

    @Test
    @DisplayName("이미 좋아요면 bump 호출 안 함")
    void 이미_좋아요면_bump_호출_안함() {
        // given - 이미 좋아요 등록된 상태
        LikeCommand.Create createCommand = new LikeCommand.Create(userId, productId);
        likeFacade.like(createCommand);
        
        // 상품 상세 캐시 생성
        productFacade.findProductById(productId);
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when - 동일한 상품에 다시 좋아요 등록 시도
        likeFacade.like(createCommand);
        
        // then - 상품 상세 캐시가 그대로 유지되어야 함 (bump 호출 안함)
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // 좋아요 수는 여전히 1이어야 함 (중복 등록 안됨)
        var productInfo = productFacade.findProductById(productId);
        assertThat(productInfo.likes()).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요 상태가 아니면(좋아요 취소를 할수없으니) bump 호출 안함")
    void 좋아요상태가_아니면_bump_호출_안함() {
        // given - 좋아요가 등록되지 않은 상태
        // 상품 상세 캐시 생성
        productFacade.findProductById(productId);
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // when & then - 좋아요가 없는 상태에서 좋아요 취소 시도하면 예외가 발생해야 함
        assertThatThrownBy(() -> {
            likeFacade.unlike(userId, productId);
        }).isInstanceOf(CoreException.class)
          .hasMessageContaining("좋아요를 찾을 수 없습니다");
        
        // 상품 상세 캐시가 그대로 유지되어야 함 (bump 호출 안함)
        assertThat(cacheManager.getCache("product").get(productId)).isNotNull();
        
        // 좋아요 수는 여전히 0이어야 함
        var productInfo = productFacade.findProductById(productId);
        assertThat(productInfo.likes()).isEqualTo(0L);
    }
}
