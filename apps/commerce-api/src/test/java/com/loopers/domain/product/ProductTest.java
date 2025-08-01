package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("제품 단위 테스트")
class ProductTest {

    @DisplayName("생성")
    @Nested
    class Create {

        @DisplayName("유효한 상품 정보를 생성할 수 있다")
        @Test
        void 유효한_상품정보를_생성한다() {
            // given
            ProductCommand.Create command = new ProductCommand.Create(
                    "티셔츠", 1L, 10000L, 10L, 0L
            );

            // when
            ProductEntity product = ProductEntity.from(command);

            // then
            assertNotNull(product);
            assertEquals(command.name(), product.getName());
            assertEquals(command.brandId(), product.getBrandId());
            assertEquals(command.price(), product.getPrice());
            assertEquals(command.stock(), product.getStock());
            assertEquals(0L, product.getLikes());
        }

        @DisplayName("상품 이름이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 상품이름이_null이면_예외발생() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Create invalidCommand = new ProductCommand.Create(
                        null,
                        1L,
                        1000L,
                        10L,
                        0L
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("브랜드 ID가 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 브랜드가_null이면_예외발생() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Create invalidCommand = new ProductCommand.Create(
                        "맥북",
                        null,
                        1000L,
                        10L,
                        0L
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("가격이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 가격이_null이면_예외발생() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Create invalidCommand = new ProductCommand.Create(
                        "수영복",
                        1L,
                        null,
                        10L,
                        0L
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("재고가 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 재고가_null이면_예외발생() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Create invalidCommand = new ProductCommand.Create(
                        "바지",
                        1L,
                        1000L,
                        null,
                        0L
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("가격이 음수이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 가격이_음수면_예외발생() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Create invalidCommand = new ProductCommand.Create(
                        "티셔츠",
                        1L,
                        -1000L,
                        10L,
                        0L
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("재고가 음수이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 재고가_음수면_예외발생() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                ProductCommand.Create invalidCommand = new ProductCommand.Create(
                        "테스트 상품",
                        1L,
                        1000L,
                        -10L,
                        0L
                );
                ProductEntity.from(invalidCommand);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }
}
