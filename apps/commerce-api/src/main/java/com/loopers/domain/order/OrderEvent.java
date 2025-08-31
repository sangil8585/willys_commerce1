package com.loopers.domain.order;

import java.time.ZonedDateTime;
import java.util.List;

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
    
    public record CouponUsageRequested(
        Long orderId,
        Long userId,
        Long couponId,
        Long orderAmount,
        ZonedDateTime requestedAt
    ) {
        public static CouponUsageRequested of(Long orderId, Long userId, Long couponId, Long orderAmount) {
            return new CouponUsageRequested(orderId, userId, couponId, orderAmount, ZonedDateTime.now());
        }
    }
    
    public record CouponUsageCompleted(
        Long orderId,
        Long userId,
        Long couponId,
        Long discountAmount,
        Long finalAmount,
        ZonedDateTime completedAt
    ) {
        public static CouponUsageCompleted of(Long orderId, Long userId, Long couponId, Long discountAmount, Long finalAmount) {
            return new CouponUsageCompleted(orderId, userId, couponId, discountAmount, finalAmount, ZonedDateTime.now());
        }
    }
    
    public record StockReservationRequested(
        Long orderId,
        Long userId,
        List<StockReservationItem> items,
        ZonedDateTime requestedAt
    ) {
        public static StockReservationRequested of(Long orderId, Long userId, List<StockReservationItem> items) {
            return new StockReservationRequested(orderId, userId, items, ZonedDateTime.now());
        }
    }
    
    public record StockReservationCompleted(
        Long orderId,
        Long userId,
        List<StockReservationItem> items,
        ZonedDateTime completedAt
    ) {
        public static StockReservationCompleted of(Long orderId, Long userId, List<StockReservationItem> items) {
            return new StockReservationCompleted(orderId, userId, items, ZonedDateTime.now());
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
    
    public record StockReservationItem(
        Long productId,
        Integer quantity
    ) {
        public static StockReservationItem of(Long productId, Integer quantity) {
            return new StockReservationItem(productId, quantity);
        }
    }
}
