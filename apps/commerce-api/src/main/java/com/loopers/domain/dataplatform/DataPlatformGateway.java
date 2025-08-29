package com.loopers.domain.dataplatform;

/**
 * 데이터 플랫폼과의 통신을 위한 도메인 인터페이스
 * 외부 시스템과의 통신은 도메인 계층에서 추상화하여 의존성 역전 원칙을 적용
 */
public interface DataPlatformGateway {
    void sendOrderData(Long orderId, Long userId, String eventType);
    void sendPaymentData(Long orderId, String paymentId, Long finalAmount);
}
