package com.loopers.domain.like;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
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
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private UserEntity testUser;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = new UserEntity("testuser", UserEntity.Gender.MALE, "1990-01-01", "test@example.com");
        testUser = userRepository.save(testUser);

        // 테스트용 상품 생성
        testProduct = new ProductEntity("Test Product", 1L, 10000L, 100L, 0L);
        testProduct = productRepository.save(testProduct);
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
}
