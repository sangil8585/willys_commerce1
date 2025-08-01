package com.loopers.application.point;

public record PointInfo(
        String userId,
        Long amount
) {
    public static PointInfo from(String userId, Long amount) {
        return new PointInfo(userId, amount);
    }
    
    public static PointInfo from(String userId, Long amount, boolean allowNull) {
        if (!allowNull && amount == null) {
            return null;
        }
        return new PointInfo(userId, amount);
    }
} 