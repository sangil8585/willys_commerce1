package com.loopers.domain.like;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private LikeRepository likeRepository;

    private UserEntity testUser;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        var testUserCommand = UserCommand.Create.of("testUser", "MALE", "2000-01-01", "sangil8585@naver.com");
        testUser = userService.signUp(testUserCommand);

        var testBrand = brandService.create("나이키").getId();

        var testProductCommand = ProductCommand.Create.of("티셔츠", testBrand, 1000L, 10L, 2L);
        testProduct = productService.createProduct(testProductCommand);
    }

    @Test
    @DisplayName("로그인하지 않은 유저면 좋아요를 실패한다")
    void 로그인_하지않은_유저는_좋아요를_실패() {
        // given
        Long nonExistentUserId = 999L;
        Long productId = testProduct.getId();

        // when & then
        assertThatThrownBy(() -> {
            likeService.createLike(new LikeCommand.Create(nonExistentUserId, productId));
        }).isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("유저가 있을때는 좋아요를 성공한다")
    void 유저가_있을때_좋아요_성공() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when
        LikeEntity like = likeService.createLike(new LikeCommand.Create(userId, productId));

        // then
        assertThat(like).isNotNull();
        assertThat(like.getUserId()).isEqualTo(userId);
        assertThat(like.getProductId()).isEqualTo(productId);
        
        // Repository를 통해 좋아요 존재 여부 확인
        boolean exists = likeRepository.existsByUserIdAndProductId(userId, productId);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("같은 유저가 같은 상품에 좋아요를 여러번 눌러도 한번만 생성된다")
    void 좋아요_멱등성_테스트() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when
        LikeEntity firstLike = likeService.createLike(new LikeCommand.Create(userId, productId));
        LikeEntity secondLike = likeService.createLike(new LikeCommand.Create(userId, productId));
        LikeEntity thirdLike = likeService.createLike(new LikeCommand.Create(userId, productId));

        // then
        assertThat(firstLike.getId()).isEqualTo(secondLike.getId());
        assertThat(secondLike.getId()).isEqualTo(thirdLike.getId());
        
        // Repository를 통해 좋아요 존재 여부 확인
        boolean exists = likeRepository.existsByUserIdAndProductId(userId, productId);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 상품에 좋아요를 누르면 실패한다")
    void 존재하지않는_상품에_좋아요를_누르면_실패() {
        // given
        Long userId = testUser.getId();
        Long nonExistentProductId = 999L;

        // when & then
        assertThatThrownBy(() -> {
            likeService.createLike(new LikeCommand.Create(userId, nonExistentProductId));
        }).isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("좋아요 존재 여부를 확인할 수 있다")
    void 좋아요_여부를_확인할수있다() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when - 좋아요 생성 전
        boolean existsBefore = likeRepository.existsByUserIdAndProductId(userId, productId);

        // then
        assertThat(existsBefore).isFalse();

        // when - 좋아요 생성 후
        likeService.createLike(new LikeCommand.Create(userId, productId));
        boolean existsAfter = likeRepository.existsByUserIdAndProductId(userId, productId);

        // then
        assertThat(existsAfter).isTrue();
    }
}
