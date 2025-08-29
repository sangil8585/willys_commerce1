package com.loopers.application.like;

import com.loopers.domain.like.LikeEntity;

public record LikeInfo(
        Long id,
        Long userId,
        Long productId
) {
    public static LikeInfo from(LikeEntity likeEntity) {
        return new LikeInfo(
            likeEntity.getId(),
            likeEntity.getUserId(),
            likeEntity.getProductId()
        );
    }
}
