package com.loopers.application.like;

import com.loopers.domain.like.LikeEntity;

public record LikeInfo(
        Long id,
        Long userId,
        String targetType
) {
    public static LikeInfo from(LikeEntity likeEntity) {
        return new LikeInfo(
                likeEntity.getId(),
                likeEntity.getUserId(),
                "PRODUCT"
        );
    }
}
