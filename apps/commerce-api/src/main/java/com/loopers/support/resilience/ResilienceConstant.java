package com.loopers.support.resilience;

public final class ResilienceConstant {
    
    public static final String PG_REQUEST_CB = "pgCircuitBreaker";
    public static final String PG_FIND_CB = "pgFindCircuitBreaker";
    
    public static final String PG_RETRY = "pgRetry";
    
    public static final String PG_TIME_LIMITER = "pgTimeLimiter";
    
    private ResilienceConstant() {
        // 유틸리티 클래스는 인스턴스화하지 않음
    }
}
