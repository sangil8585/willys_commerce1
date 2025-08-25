package com.loopers.domain.payment;

public class PaymentCommand {
    
    public record Create(
        Long userId,
        String orderId,
        String cardType,
        String cardNo,
        String amount,
        String callbackUrl
    ) {
        public static Create of(
                Long userId,
                String orderId,
                String cardType,
                String cardNo,
                String amount,
                String callbackUrl
        ) {
            return new Create(userId, orderId, cardType, cardNo, amount, callbackUrl);
        }
    }
    
    public record AssignPaymentId(
        String paymentId
    ) {
        public static AssignPaymentId of(String paymentId) {
            return new AssignPaymentId(paymentId);
        }
    }
    
    public record UpdateStatus(
        PaymentStatus status
    ) {
        public static UpdateStatus of(PaymentStatus status) {
            return new UpdateStatus(status);
        }
    }
    
    public record Complete(
        String transactionId
    ) {
        public static Complete of(String transactionId) {
            return new Complete(transactionId);
        }
    }
    
    public record Fail(
        String errorMessage
    ) {
        public static Fail of(String errorMessage) {
            return new Fail(errorMessage);
        }
    }
    
    public record Cancel(
        String reason
    ) {
        public static Cancel of(String reason) {
            return new Cancel(reason);
        }
    }
}
