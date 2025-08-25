package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;

public class PaymentV1Dto {
    
    public record PaymentRequest(  
        String orderId,
        String cardType,
        String cardNo,
        String amount,
        String callbackUrl
    ) {
    }

    public record PaymentResponse(
        String orderId,
        String paymentId,
        String status,
        String message
    ) {
        public static PaymentResponse from(PaymentResult result) {
            return new PaymentResponse(
                result.orderId(),
                result.paymentId(),
                result.status(),
                "결제 요청이 성공적으로 접수되었습니다."
            );
        }
    }
    
    public record PaymentInfoResponse(
        String paymentId,
        String orderId,
        String cardType,
        String cardNo,
        String amount,
        String status,
        String callbackUrl,
        String createdAt
    ) {
    }
}
