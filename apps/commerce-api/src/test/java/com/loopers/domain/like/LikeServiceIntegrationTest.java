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

import java.util.Optional;

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

        Optional<LikeEntity> foundLike = likeRepository.findByUserIdAndProductId(userId, productId);
        assertThat(foundLike).isPresent();
        assertThat(foundLike.get().getId()).isEqualTo(like.getId());
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

        Optional<LikeEntity> foundLike = likeRepository.findByUserIdAndProductId(userId, productId);
        assertThat(foundLike).isPresent();
        assertThat(foundLike.get().getId()).isEqualTo(firstLike.getId());
    }

    @Test
    @DisplayName("좋아요 존재 여부를 확인할 수 있다")
    void 좋아요_여부를_확인할수있다() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when - 좋아요 생성 전
        boolean existsBefore = likeService.existsByUserIdAndProductId(userId, productId);

        // then
        assertThat(existsBefore).isFalse();

        // when - 좋아요 생성 후
        LikeEntity createdLike = likeService.createLike(new LikeCommand.Create(userId, productId));
        boolean existsAfter = likeService.existsByUserIdAndProductId(userId, productId);

        // then
        assertThat(existsAfter).isTrue();
    }

    @Test
    @DisplayName("좋아요를 삭제할 수 있다")
    void 좋아요_삭제_테스트() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();
        LikeEntity createdLike = likeService.createLike(new LikeCommand.Create(userId, productId));

        // when
        likeService.removeLike(userId, productId);

        // then
        Optional<LikeEntity> foundLike = likeRepository.findByUserIdAndProductId(userId, productId);
        assertThat(foundLike).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 좋아요를 삭제하려고 하면 예외가 발생한다")
    void 존재하지않는_좋아요_삭제_예외_테스트() {
        // given
        Long userId = testUser.getId();
        Long productId = testProduct.getId();

        // when & then
        assertThatThrownBy(() -> {
            likeService.removeLike(userId, productId);
        }).isInstanceOf(CoreException.class);
    }
}
