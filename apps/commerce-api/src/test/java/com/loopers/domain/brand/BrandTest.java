package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BrandTest {

    @DisplayName("브랜드 생성")
    @Nested
    class Create {

        @DisplayName("유효한 브랜드명으로 BrandEntity를 생성할 수 있다")
        @Test
        void 유효한_브랜드명으로_생성() {
            // given
            String brandName = "테스트 브랜드";

            // when
            BrandEntity brand = BrandEntity.of(brandName);

            // then
            assertNotNull(brand);
            assertEquals(brandName, brand.getName());
        }

        @DisplayName("브랜드명이 비어있으면, BAD_REQUEST 예외를 던진다.")
        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void 브랜드명이_비어있으면_예외를_발생한다(String invalidBrandName) {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                BrandEntity.of(invalidBrandName);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @DisplayName("브랜드명이 null이면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 브랜드명이_null이면_예외를_발생한다() {
            // given

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                BrandEntity.of(null);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }

    @DisplayName("브랜드 수정")
    @Nested
    class UpdateName {

        @DisplayName("유효한 브랜드명으로 수정할 수 있다")
        @Test
        void 유효한_브랜드명으로_수정한다() {
            // given
            BrandEntity brand = BrandEntity.of("기존 브랜드");
            String newName = "새로운 브랜드";

            // when
            brand.updateName(newName);

            // then
            assertEquals(newName, brand.getName());
        }

        @DisplayName("브랜드명을 null로 수정하면, BAD_REQUEST 예외를 던진다.")
        @Test
        void 브랜드를_null로_수정시_예외를_발생한다() {
            // given
            BrandEntity brand = BrandEntity.of("기존 브랜드");

            // when
            CoreException exception = assertThrows(CoreException.class, () -> {
                brand.updateName(null);
            });

            // then
            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }
    }
}
