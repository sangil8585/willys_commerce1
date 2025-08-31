package com.loopers.application.like;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private LikeService likeService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private UserEntity testUser;
    private ProductEntity testProduct;

    @BeforeEach
    void setUp() {
        var testUserCommand = UserCommand.Create.of(1L, "MALE", "2000-01-01", "sangil8585@naver.com");
        testUser = userService.signUp(testUserCommand);

        var testBrand = brandService.create("나이키").getId();

        var testProductCommand = ProductCommand.Create.of("티셔츠", testBrand, 1000L, 10L, 0L);
        testProduct = productService.createProduct(testProductCommand);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 좋아요를 누르면 실패한다")
    void 존재하지않는_사용자_좋아요_실패() {
        // given
        Long nonExistentUserId = 999L;
        Long productId = testProduct.getId();

        // when & then
        assertThatThrownBy(() -> {
            likeFacade.like(new LikeCommand.Create(nonExistentUserId, productId));
        }).isInstanceOf(CoreException.class)
          .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 상품에 좋아요를 누르면 실패한다")
    void 존재하지않는_상품_좋아요_실패() {
        // given
        Long userId = testUser.getId();
        Long nonExistentProductId = 999L;

        // when & then
        assertThatThrownBy(() -> {
            likeFacade.like(new LikeCommand.Create(userId, nonExistentProductId));
        }).isInstanceOf(CoreException.class)
          .hasMessageContaining("존재하지 않는 상품입니다.");
    }

//    @Test
//    @DisplayName("정상적인 좋아요 생성 시 상품의 좋아요 카운트가 증가한다")
//    void 좋아요_생성_시_상품_좋아요_카운트_증가() {
//        // given
//        Long userId = testUser.getId();
//        Long productId = testProduct.getId();
//        Long initialLikes = testProduct.getLikes();
//
//        // when
//        LikeInfo likeInfo = likeFacade.like(new LikeCommand.Create(userId, productId));
//
//        // then
//        assertThat(likeInfo).isNotNull();
//        assertThat(likeInfo.userId()).isEqualTo(userId);
//        assertThat(likeInfo.productId()).isEqualTo(productId);
//
//        // 상품의 좋아요 카운트가 증가했는지 확인
//        ProductEntity updatedProduct = productService.findById(productId).orElseThrow();
//        assertThat(updatedProduct.getLikes()).isEqualTo(initialLikes + 1);
//    }

//    @Test
//    @DisplayName("같은 사용자가 같은 상품에 좋아요를 여러번 눌러도 상품의 좋아요 카운트는 한번만 증가한다")
//    void 좋아요_멱등성_상품_카운트_테스트() {
//        // given
//        Long userId = testUser.getId();
//        Long productId = testProduct.getId();
//        Long initialLikes = testProduct.getLikes();
//
//        // when
//        LikeInfo firstLike = likeFacade.like(new LikeCommand.Create(userId, productId));
//        LikeInfo secondLike = likeFacade.like(new LikeCommand.Create(userId, productId));
//        LikeInfo thirdLike = likeFacade.like(new LikeCommand.Create(userId, productId));
//
//        // then
//        // 멱등성 확인: 같은 사용자가 같은 상품에 좋아요를 여러번 눌러도 하나의 레코드만 생성
//        assertThat(firstLike.userId()).isEqualTo(secondLike.userId());
//        assertThat(firstLike.productId()).isEqualTo(secondLike.productId());
//
//        // 상품의 좋아요 카운트는 한번만 증가했는지 확인
//        ProductEntity updatedProduct = productService.findById(productId).orElseThrow();
//        assertThat(updatedProduct.getLikes()).isEqualTo(initialLikes + 1);
//    }

//    @Test
//    @DisplayName("좋아요 취소 시 상품의 좋아요 카운트가 감소한다")
//    void 좋아요_취소_시_상품_좋아요_카운트_감소() {
//        // given
//        Long userId = testUser.getId();
//        Long productId = testProduct.getId();
//
//        // 좋아요 생성
//        likeFacade.like(new LikeCommand.Create(userId, productId));
//        ProductEntity productAfterLike = productService.findById(productId).orElseThrow();
//        Long likesAfterLike = productAfterLike.getLikes();
//
//        // when
//        likeFacade.unlike(userId, productId);
//
//        // then
//        ProductEntity productAfterUnlike = productService.findById(productId).orElseThrow();
//        assertThat(productAfterUnlike.getLikes()).isEqualTo(likesAfterLike - 1);
//    }

    @Test
    @DisplayName("존재하지 않는 사용자로 좋아요 취소를 하면 실패한다")
    void 존재하지않는_사용자_좋아요_취소_실패() {
        // given
        Long nonExistentUserId = 999L;
        Long productId = testProduct.getId();

        // when & then
        assertThatThrownBy(() -> {
            likeFacade.unlike(nonExistentUserId, productId);
        }).isInstanceOf(CoreException.class)
          .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 상품에 좋아요 취소를 하면 실패한다")
    void 존재하지않는_상품_좋아요_취소_실패() {
        // given
        Long userId = testUser.getId();
        Long nonExistentProductId = 999L;

        // when & then
        assertThatThrownBy(() -> {
            likeFacade.unlike(userId, nonExistentProductId);
        }).isInstanceOf(CoreException.class)
          .hasMessageContaining("존재하지 않는 상품입니다.");
    }

    /**
     * ### ❤️ 좋아요 ↔ 집계
     *
     * - [x]  **이벤트 기반**으로 좋아요 처리와 집계를 분리한다.
     * - [x]  집계 로직의 성공/실패와 상관 없이, 좋아요 처리는 정상적으로 완료되어야 한다.
     */
    @DisplayName("이벤트 기반 분리 테스트")
    @Nested
    class EventBasedSeparationTest {

        @Test
        @DisplayName("좋아요 처리가 이벤트로 분리되어 즉시 반환된다")
        void 좋아요_이벤트_분리_테스트() throws InterruptedException {
            // given
            Long userId = testUser.getId();
            Long productId = testProduct.getId();
            Long initialLikes = testProduct.getLikes();

            // when
            LikeInfo likeInfo = likeFacade.like(new LikeCommand.Create(userId, productId));

            // then
            // 1. 즉시 응답 반환 (이벤트 발행만)
            assertThat(likeInfo).isNotNull();
            assertThat(likeInfo.userId()).isEqualTo(userId);
            assertThat(likeInfo.productId()).isEqualTo(productId);

            // 2. 잠시 대기 후 실제 좋아요 처리 확인 (이벤트 핸들러에서 처리)
            Thread.sleep(1000);

            // 3. 실제 좋아요가 생성되었는지 확인
            boolean likeExists = likeService.existsByUserIdAndProductId(userId, productId);
            assertThat(likeExists).isTrue();

            // 4. 상품의 좋아요 카운트가 업데이트되었는지 확인
            ProductEntity updatedProduct = productService.findById(productId).orElseThrow();
            assertThat(updatedProduct.getLikes()).isEqualTo(initialLikes + 1);
        }

        @Test
        @DisplayName("좋아요 취소가 이벤트로 분리되어 즉시 반환된다")
        void 좋아요_취소_이벤트_분리_테스트() throws InterruptedException {
            // given
            Long userId = testUser.getId();
            Long productId = testProduct.getId();
            
            // 좋아요 생성
            likeFacade.like(new LikeCommand.Create(userId, productId));
            Thread.sleep(1000); // 이벤트 처리 대기
            
            ProductEntity productAfterLike = productService.findById(productId).orElseThrow();
            Long likesAfterLike = productAfterLike.getLikes();
            assertThat(likesAfterLike).isEqualTo(1L);

            // when
            likeFacade.unlike(userId, productId);

            // then
            // 1. 즉시 반환 (이벤트 발행만)
            // 2. 잠시 대기 후 실제 좋아요 삭제 확인
            Thread.sleep(1000);

            // 3. 실제 좋아요가 삭제되었는지 확인
            boolean likeExists = likeService.existsByUserIdAndProductId(userId, productId);
            assertThat(likeExists).isFalse();

            // 4. 상품의 좋아요 카운트가 감소했는지 확인
            ProductEntity productAfterUnlike = productService.findById(productId).orElseThrow();
            assertThat(productAfterUnlike.getLikes()).isEqualTo(likesAfterLike - 1);
        }
    }

    @DisplayName("집계 로직 격리 테스트")
    @Nested
    class AggregationIsolationTest {

        @Test
        @DisplayName("집계 로직 실패 시에도 좋아요 처리는 정상적으로 완료된다")
        void 집계_로직_실패_시에도_좋아요_처리_완료() throws InterruptedException {
            // given
            Long userId = testUser.getId();
            Long productId = testProduct.getId();
            Long initialLikes = testProduct.getLikes();

            // when
            LikeInfo likeInfo = likeFacade.like(new LikeCommand.Create(userId, productId));

            // then
            // 좋아요 요청은 즉시 성공
            assertThat(likeInfo).isNotNull();
            assertThat(likeInfo.userId()).isEqualTo(userId);
            assertThat(likeInfo.productId()).isEqualTo(productId);

            // 잠시 대기 후 실제 좋아요 처리 확인
            Thread.sleep(1000);

            // 좋아요가 실제로 생성되었는지 확인 (집계 로직과 무관하게 처리 되도록 함)
            boolean likeExists = likeService.existsByUserIdAndProductId(userId, productId);
            assertThat(likeExists).isTrue();

            // 상품의 좋아요 카운트도 정상적으로 업데이트되었는지 확인
            ProductEntity updatedProduct = productService.findById(productId).orElseThrow();
            assertThat(updatedProduct.getLikes()).isEqualTo(initialLikes + 1);
        }

        @Test
        @DisplayName("좋아요 취소 시에도 집계 로직과 독립적으로 처리된다")
        void 좋아요_취소_집계_독립성_테스트() throws InterruptedException {
            // given
            Long userId = testUser.getId();
            Long productId = testProduct.getId();
            
            // 좋아요 생성
            likeFacade.like(new LikeCommand.Create(userId, productId));
            Thread.sleep(1000);
            
            ProductEntity productAfterLike = productService.findById(productId).orElseThrow();
            Long likesAfterLike = productAfterLike.getLikes();
            assertThat(likesAfterLike).isEqualTo(1L);

            // when
            likeFacade.unlike(userId, productId);

            // then
            Thread.sleep(1000);

            // 좋아요가 실제로 삭제되었는지 확인 (집계 로직과 무관하게)
            boolean likeExists = likeService.existsByUserIdAndProductId(userId, productId);
            assertThat(likeExists).isFalse();

            // 상품의 좋아요 카운트도 정상적으로 감소했는지 확인
            ProductEntity productAfterUnlike = productService.findById(productId).orElseThrow();
            assertThat(productAfterUnlike.getLikes()).isEqualTo(likesAfterLike - 1);
        }
    }

    @DisplayName("좋아요/싫어요 동시성 테스트")
    @Nested
    class LikeConcurrencyTest {

        @Test
        @DisplayName("동일한 상품에 대해 여러명이 좋아요/싫어요를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다")
        void 동시_좋아요_싫어요_테스트() throws Exception {
            // given
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        // 짝수 인덱스는 좋아요(+1), 홀수 인덱스는 좋아요 취소(-1)
                        if (index % 2 == 0) {
                            // 좋아요 생성
                            likeFacade.like(new LikeCommand.Create(testUser.getId(), testProduct.getId()));
                        } else {
                            // 좋아요 취소 (이미 좋아요가 있는 상태에서)
                            try {
                                likeFacade.unlike(testUser.getId(), testProduct.getId());
                            } catch (Exception e) {
                                // 좋아요가 없는 경우 무시
                            }
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Thread " + index + " failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // then
            ProductEntity updatedProduct = productService.findById(testProduct.getId()).orElseThrow();
            // 10개 스레드: 짝수(5개)는 +1, 홀수(5개)는 -1 → 결과: 5-5=0
            assertThat(updatedProduct.getLikes()).isEqualTo(0L);
            assertThat(successCount.get()).isEqualTo(10);
            assertThat(failureCount.get()).isEqualTo(0);
        }
    }
} 
