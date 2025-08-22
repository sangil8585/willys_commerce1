package com.loopers.domain.payment;

public record PaymentInfo(
    String transactionKey,
    String orderId,
    String cardType,
    String cardNo,
    Long amount,
    String status,
    String reason
) {
    public static PaymentInfo of(String transactionKey, String orderId, String cardType, String cardNo, Long amount, String status, String reason) {
        return new PaymentInfo(transactionKey, orderId, cardType, cardNo, amount, status, reason);
    }
}
