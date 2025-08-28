package com.loopers.domain.order;

import java.time.ZonedDateTime;

public abstract class OrderEvent {
    
    public record Completed(
        Long orderId,
        Long userId,
        Long totalAmount,
        Long discountAmount,
        ZonedDateTime createdAt
    ) {
        public static Completed of(Long orderId, Long userId, Long totalAmount, Long discountAmount) {
            return new Completed(orderId, userId, totalAmount, discountAmount, ZonedDateTime.now());
        }
    }
    
    public record PaymentCompleted(
        Long orderId,
        Long userId,
        String paymentId,
        Long finalAmount,
        ZonedDateTime completedAt
    ) {
        public static PaymentCompleted of(Long orderId, Long userId, String paymentId, Long finalAmount) {
            return new PaymentCompleted(orderId, userId, paymentId, finalAmount, ZonedDateTime.now());
        }
    }
    
    public record DataPlatformSent(
        Long orderId,
        Long userId,
        String eventType,
        ZonedDateTime sentAt
    ) {
        public static DataPlatformSent of(Long orderId, Long userId, String eventType) {
            return new DataPlatformSent(orderId, userId, eventType, ZonedDateTime.now());
        }
    }
}
