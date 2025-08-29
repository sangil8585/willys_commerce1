package com.loopers.domain.like;

import java.time.ZonedDateTime;

public abstract class LikeEvent {
    
    public record Created(
        Long userId,
        Long productId
    ) {
        public static Created of(Long userId, Long productId) {
            return new Created(userId, productId);
        }
    }
    
    public record Removed(
        Long userId,
        Long productId
    ) {
        public static Removed of(Long userId, Long productId) {
            return new Removed(userId, productId);
        }
    }
}
