package com.loopers.application.point;

public record PointInfo(
        Long userId,
        Long amount
) {
    public static PointInfo from(Long userId, Long amount) {
        return new PointInfo(userId, amount);
    }
    
    public static PointInfo from(Long userId, Long amount, boolean allowNull) {
        if (!allowNull && amount == null) {
            return null;
        }
        return new PointInfo(userId, amount);
    }
} 