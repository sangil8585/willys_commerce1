package com.loopers.domain.like;

public class LikeCommand {
    public record Create(
        Long userId,
        Long productId
    ) {
    }

    public record Remove(
        Long userId,
        Long productId
    ) {
    }
}
