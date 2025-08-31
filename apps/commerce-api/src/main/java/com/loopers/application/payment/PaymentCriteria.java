package com.loopers.application.payment;

public record PaymentCriteria(
    Long userId,
    String orderId,
    String cardType,
    String cardNo,
    String amount,
    String callbackUrl
) {
    
}