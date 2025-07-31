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

    private UserEntity testUser;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        var testUserCommand = UserCommand.Create.of("testUser", "MALE", "2000-01-01", "sangil8585@naver.com");
        testUser = userService.signUp(testUserCommand);

        // 테스트용 브랜드 생성
        var testBrand = brandService.create("나이키").getId();

        // 테스트용 상품 생성
        var testProductCommand = ProductCommand.Create.of("티셔츠", testBrand, 1000L, 10L, 2L);
        testProduct = productService.createProduct(testProductCommand);
    }

    @Test
    @DisplayName("로그인하지 않은 유저면 좋아요를 실패한다")
    void shouldFailWhenUserNotLoggedIn() {
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
    void shouldSucceedWhenUserExists() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when
        LikeEntity like = likeService.createLike(new LikeCommand.Create(userId, productId));

        // then
        assertThat(like).isNotNull();
        assertThat(like.getUserId()).isEqualTo(userId);
        assertThat(like.getProductId()).isEqualTo(productId);
        assertThat(likeService.existsByUserIdAndProductId(userId, productId)).isTrue();
    }

    @Test
    @DisplayName("같은 유저가 같은 상품에 좋아요를 여러번 눌러도 한번만 생성된다")
    void shouldCreateLikeOnlyOnceWhenUserLikesSameProductMultipleTimes() {
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
        assertThat(likeService.existsByUserIdAndProductId(userId, productId)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 상품에 좋아요를 누르면 실패한다")
    void shouldFailWhenProductDoesNotExist() {
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
    void shouldCheckLikeExistenceSuccessfully() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when - 좋아요 생성 전
        boolean existsBefore = likeService.existsByUserIdAndProductId(userId, productId);

        // then
        assertThat(existsBefore).isFalse();

        // when - 좋아요 생성 후
        likeService.createLike(new LikeCommand.Create(userId, productId));
        boolean existsAfter = likeService.existsByUserIdAndProductId(userId, productId);

        // then
        assertThat(existsAfter).isTrue();
    }

    @Test
    @DisplayName("다른 유저가 같은 상품에 좋아요를 누를 수 있다")
    void shouldAllowDifferentUsersToLikeSameProduct() {
        // given
        Long firstUserId = testUser.getId();
        Long productId = testProduct.getId();

        // 두 번째 유저 생성
        var secondUserCommand = UserCommand.Create.of("testUser2", "FEMALE", "1995-05-05", "test2@naver.com");
        UserEntity secondUser = userService.signUp(secondUserCommand);
        Long secondUserId = secondUser.getId();

        // when
        LikeEntity firstLike = likeService.createLike(new LikeCommand.Create(firstUserId, productId));
        LikeEntity secondLike = likeService.createLike(new LikeCommand.Create(secondUserId, productId));

        // then
        assertThat(firstLike).isNotNull();
        assertThat(secondLike).isNotNull();
        assertThat(firstLike.getId()).isNotEqualTo(secondLike.getId());
        assertThat(likeService.existsByUserIdAndProductId(firstUserId, productId)).isTrue();
        assertThat(likeService.existsByUserIdAndProductId(secondUserId, productId)).isTrue();
    }
}
