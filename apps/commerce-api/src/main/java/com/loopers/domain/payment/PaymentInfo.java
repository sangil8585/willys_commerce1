package com.loopers.domain.payment;

import java.time.ZonedDateTime;

/**
 * 결제 정보를 나타내는 Info 클래스
 */
public record PaymentInfo(
    Long id,
    Long userId,
    String orderId,
    String paymentId,
    String cardType,
    String cardNo,
    String amount,
    String callbackUrl,
    PaymentStatus status,
    String transactionId,
    ZonedDateTime processedAt,
    String errorMessage,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {
    
    public static PaymentInfo from(PaymentEntity paymentEntity) {
        return new PaymentInfo(
            paymentEntity.getId(),
            paymentEntity.getUserId(),
            paymentEntity.getOrderId(),
            paymentEntity.getPaymentId(),
            paymentEntity.getCardType(),
            paymentEntity.getCardNo(),
            paymentEntity.getAmount(),
            paymentEntity.getCallbackUrl(),
            paymentEntity.getStatus(),
            paymentEntity.getTransactionId(),
            paymentEntity.getProcessedAt(),
            paymentEntity.getErrorMessage(),
            paymentEntity.getCreatedAt(),
            paymentEntity.getUpdatedAt()
        );
    }
}
