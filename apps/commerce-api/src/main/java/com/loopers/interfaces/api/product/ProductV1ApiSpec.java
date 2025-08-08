package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product V1 API", description = "상품 관련 API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 조회합니다."
    )
    ApiResponse<ProductV1Dto.V1.GetProductListResponse> getProductList(
            @Schema(name = "브랜드 ID", description = "브랜드별 필터링")
            Long brandId,
            @Schema(name = "정렬", description = "정렬 기준 (latest, price_asc, price_desc)")
            String sort,
            @Schema(name = "페이지", description = "페이지 번호 (0부터 시작)")
            Integer page,
            @Schema(name = "크기", description = "페이지당 상품 수")
            Integer size
    );

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID로 상품 상세 정보를 조회합니다."
    )
    ApiResponse<ProductV1Dto.V1.ProductResponse> getProduct(
            @Schema(name = "상품 ID", description = "조회할 상품의 ID")
            Long productId
    );
} 