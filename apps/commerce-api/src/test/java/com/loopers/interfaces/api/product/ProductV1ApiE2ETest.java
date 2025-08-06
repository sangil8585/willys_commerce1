package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductV1ApiE2ETest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    private static final String ENDPOINT = "/api/v1/products";


    private BrandEntity createBrand() {
        return brandService.create("APC");
    }

    private ProductEntity createProduct(String name) {
        BrandEntity brand = createBrand();
        ProductCommand.Create createCommand = new ProductCommand.Create(
                name,
                brand.getId(),
                10000L,
                10L,
                0L
        );
        return productService.createProduct(createCommand);
    }

    private ProductEntity createProductWithBrand(String name, BrandEntity brand) {
        ProductCommand.Create createCommand = new ProductCommand.Create(
                name,
                brand.getId(),
                10000L,
                10L,
                0L
        );
        return productService.createProduct(createCommand);
    }

    @DisplayName("상품 목록 조회 성공시 상품목록 정보를 반환")
    @Test
    void 상품조회_성공시_상품목록_반환() {
        //given
        BrandEntity brand = createBrand();
        ProductEntity product1 = createProductWithBrand("바지", brand);
        ProductEntity product2 = createProductWithBrand("티셔츠", brand);
        ProductEntity product3 = createProductWithBrand("자켓", brand);

        //when
        ParameterizedTypeReference<ApiResponse<ProductV1Dto.V1.GetProductListResponse>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<ApiResponse<ProductV1Dto.V1.GetProductListResponse>> response =
                testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, new HttpEntity<>(null), responseType);


        //then
        assertAll(
                () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data()).isNotNull(),
                () -> assertThat(response.getBody().data().products()).hasSize(3),
                () -> assertThat(response.getBody().data().totalElements()).isEqualTo(3L),
                () -> assertThat(response.getBody().data().totalPages()).isEqualTo(1),
                () -> assertThat(response.getBody().data().products())
                        .extracting("name")
                        .containsExactlyInAnyOrder("바지", "티셔츠", "자켓"),
                () -> assertThat(response.getBody().data().products())
                        .extracting("price")
                        .containsOnly(10000L),
                () -> assertThat(response.getBody().data().products())
                        .extracting("stock")
                        .containsOnly(10L),
                () -> assertThat(response.getBody().data().products())
                        .extracting("likes")
                        .containsOnly(0L)
        );
    }

    @DisplayName("상품 상세 조회 성공시 상품 상세 정보를 반환")
    @Test
    void 상품상세조회_성공시_상품상세정보를_반환() {
        //given
        ProductEntity product = createProduct("데님 청바지");

        //when
        ParameterizedTypeReference<ApiResponse<ProductV1Dto.V1.ProductResponse>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<ApiResponse<ProductV1Dto.V1.ProductResponse>> response =
                testRestTemplate.exchange(ENDPOINT + "/" + product.getId(), HttpMethod.GET, new HttpEntity<>(null), responseType);

        //then
        assertAll(
                () -> assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data()).isNotNull(),
                () -> assertThat(response.getBody().data().id()).isEqualTo(product.getId()),
                () -> assertThat(response.getBody().data().name()).isEqualTo("데님 청바지"),
                () -> assertThat(response.getBody().data().brandId()).isEqualTo(product.getBrandId()),
                () -> assertThat(response.getBody().data().brandName()).isEqualTo("APC"),
                () -> assertThat(response.getBody().data().price()).isEqualTo(10000L),
                () -> assertThat(response.getBody().data().stock()).isEqualTo(10L),
                () -> assertThat(response.getBody().data().likes()).isEqualTo(0L)
        );
    }
}
