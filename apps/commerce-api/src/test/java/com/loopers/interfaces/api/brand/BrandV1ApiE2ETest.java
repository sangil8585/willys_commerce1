package com.loopers.interfaces.api.brand;


import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrandV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private BrandService brandService;


    @AfterEach
    void tearDown() {
        brandJpaRepository.deleteAll();
    }

    private BrandEntity createBrand(String brandName) {
        final BrandEntity brand = brandService.create(brandName);
        return brandJpaRepository.save(brand);
    }

    @DisplayName("브랜드 조회")
    @Nested
    class Get {

        private static final String ENDPOINT = "/api/v1/brands/";

        @DisplayName("브랜드 조회에 성공할 경우, 해당하는 브랜드 정보를 응답으로 반환한다.")
        @Test
        void 브랜드_조회성공시_브랜드정보_응답() {
            // given
            BrandEntity brand = createBrand("APC");

            // when
            ParameterizedTypeReference<ApiResponse<BrandV1Dto.V1.BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandV1Dto.V1.BrandResponse>> response =
                    testRestTemplate.exchange(ENDPOINT + brand.getId(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), responseType);

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().id()).isEqualTo(brand.getId());
            assertThat(response.getBody().data().name()).isEqualTo(brand.getName());
        }
    }
}
