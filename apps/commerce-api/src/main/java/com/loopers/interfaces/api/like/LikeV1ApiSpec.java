package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Like V1 API", description = "좋아요 관련 API 입니다.")
public interface LikeV1ApiSpec {

    @Operation(
            summary = "상품 좋아요 등록",
            description = "상품에 좋아요를 등록합니다."
    )
    ApiResponse<LikeV1Dto.LikeResponse> likeProduct(
            @Schema(name = "사용자 ID", description = "좋아요를 누를 사용자의 ID")
            Long userId,
            @Schema(name = "상품 ID", description = "좋아요를 누를 상품의 ID")
            Long productId
    );

    @Operation(
            summary = "상품 좋아요 취소",
            description = "상품의 좋아요를 취소합니다."
    )
    ApiResponse<Void> removeLikeProduct(
            @Schema(name = "사용자 ID", description = "좋아요를 취소할 사용자의 ID")
            Long userId,
            @Schema(name = "상품 ID", description = "좋아요를 취소할 상품의 ID")
            Long productId
    );

    @Operation(
            summary = "사용자가 좋아요한 상품 목록 조회",
            description = "특정 사용자가 좋아요한 상품 목록을 조회합니다."
    )
    ApiResponse<List<LikeV1Dto.LikeResponse>> getLikedProducts(
            @Schema(name = "사용자 ID", description = "좋아요한 상품을 조회할 사용자의 ID")
            Long userId
    );
}
