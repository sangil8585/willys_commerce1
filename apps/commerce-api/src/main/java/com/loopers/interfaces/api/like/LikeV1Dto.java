package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public class LikeV1Dto {

    @Schema(name = "좋아요 응답")
    public record LikeResponse(
            @Schema(description = "좋아요 ID")
            Long id,
            @Schema(description = "사용자 ID")
            Long userId,
            @Schema(description = "상품 ID")
            Long productId
    ) {
        public static LikeResponse from(LikeInfo likeInfo) {
            return new LikeResponse(
                    likeInfo.id(),
                    likeInfo.userId(),
                    likeInfo.productId()
            );
        }
    }
}
