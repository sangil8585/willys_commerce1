package com.loopers.domain.brand;

import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest
public class BrandServiceIntegrationTest {
    @Autowired
    private BrandService brandService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("브랜드 조회")
    @Nested
    class Find {

        @DisplayName("브랜드 ID로 브랜드를 조회할 수 있다")
        @Test
        void 브랜드ID로_조회() {
            // given
            BrandEntity createdBrand = brandService.create("나이키");

            // when
            Optional<BrandEntity> result = brandService.find(createdBrand.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("나이키");
        }

        @DisplayName("존재하지 않는 브랜드 ID로 조회하면 빈 Optional을 반환한다")
        @Test
        void 존재하지_않는_브랜드_조회() {
            // given
            Long brandId = 999L;

            // when
            Optional<BrandEntity> result = brandService.find(brandId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("브랜드 생성")
    @Nested
    class Create {

        @DisplayName("브랜드를 생성할 수 있다")
        @Test
        void 유효한_브랜드명으로_생성() {
            // given
            String brandName = "아페쎄";

            // when
            BrandEntity result = brandService.create(brandName);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo(brandName);
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @DisplayName("브랜드명이 null이면 예외가 발생한다")
        @Test
        void 브랜드명_null_예외() {
            // given & when & then
            assertThatThrownBy(() -> brandService.create(null))
                    .isInstanceOf(RuntimeException.class);
        }
    }



}
