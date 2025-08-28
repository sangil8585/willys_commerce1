package com.loopers.domain.like;

import java.time.ZonedDateTime;

public abstract class LikeEvent {
    
    public record Created(
        Long likeId,
        Long userId,
        Long productId,
        ZonedDateTime createdAt
    ) {
        public static Created of(Long likeId, Long userId, Long productId) {
            return new Created(likeId, userId, productId, ZonedDateTime.now());
        }
    }
    
    public record Removed(
        Long likeId,
        Long userId,
        Long productId,
        ZonedDateTime removedAt
    ) {
        public static Removed of(Long likeId, Long userId, Long productId) {
            return new Removed(likeId, userId, productId, ZonedDateTime.now());
        }
    }
    
    public record AggregationRequested(
        Long productId,
        Long currentLikes,
        ZonedDateTime requestedAt
    ) {
        public static AggregationRequested of(Long productId, Long currentLikes) {
            return new AggregationRequested(productId, currentLikes, ZonedDateTime.now());
        }
    }
}
