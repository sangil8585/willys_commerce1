package com.loopers.domain.user;

import java.time.ZonedDateTime;
import java.util.Map;

public abstract class UserActivityEvent {
    
    public record ProductViewed(
        Long userId,
        Long productId,
        String userAgent,
        String ipAddress,
        ZonedDateTime viewedAt
    ) {
        public static ProductViewed of(Long userId, Long productId, String userAgent, String ipAddress) {
            return new ProductViewed(userId, productId, userAgent, ipAddress, ZonedDateTime.now());
        }
    }
    
    public record ProductClicked(
        Long userId,
        Long productId,
        String clickType,
        String userAgent,
        String ipAddress,
        ZonedDateTime clickedAt
    ) {
        public static ProductClicked of(Long userId, Long productId, String clickType, String userAgent, String ipAddress) {
            return new ProductClicked(userId, productId, clickType, userAgent, ipAddress, ZonedDateTime.now());
        }
    }
    
    public record OrderPlaced(
        Long userId,
        Long orderId,
        Long totalAmount,
        String userAgent,
        String ipAddress,
        ZonedDateTime orderedAt
    ) {
        public static OrderPlaced of(Long userId, Long orderId, Long totalAmount, String userAgent, String ipAddress) {
            return new OrderPlaced(userId, orderId, totalAmount, userAgent, ipAddress, ZonedDateTime.now());
        }
    }
    
    public record LikeAction(
        Long userId,
        Long productId,
        String actionType,
        String userAgent,
        String ipAddress,
        ZonedDateTime actionAt
    ) {
        public static LikeAction of(Long userId, Long productId, String actionType, String userAgent, String ipAddress) {
            return new LikeAction(userId, productId, actionType, userAgent, ipAddress, ZonedDateTime.now());
        }
    }
}
