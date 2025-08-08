package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Brand V1 API")
public interface BrandV1ApiSpec {

    @Operation(
            summary = "브랜드 정보 조회"
    )
    ApiResponse<BrandV1Dto.V1.BrandResponse> getBrand(
            @Schema(name = "브랜드 조회")
            Long brandId
    );
}
