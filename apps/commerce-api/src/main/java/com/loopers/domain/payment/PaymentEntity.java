package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;
    
    @Column(name = "card_type", nullable = false)
    private String cardType;
    
    @Column(name = "card_no", nullable = false)
    private String cardNo;
    
    @Column(name = "amount", nullable = false)
    private String amount;
    
    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "processed_at")
    private ZonedDateTime processedAt;
    
    @Column(name = "error_message")
    private String errorMessage;

    protected PaymentEntity() {}

    public static PaymentEntity from(PaymentCommand.Create command) {
        validateCreateCommand(command);
        
        PaymentEntity payment = new PaymentEntity();
        payment.userId = command.userId();
        payment.orderId = command.orderId();
        payment.cardType = command.cardType();
        payment.cardNo = maskCardNumber(command.cardNo());
        payment.amount = command.amount();
        payment.callbackUrl = command.callbackUrl();
        payment.status = PaymentStatus.PENDING;
        
        return payment;
    }
    
    private static void validateCreateCommand(PaymentCommand.Create command) {
        if (command.userId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (command.orderId() == null || command.orderId().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
        }
        if (command.cardType() == null || command.cardType().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 타입은 필수입니다.");
        }
        if (command.cardNo() == null || command.cardNo().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 필수입니다.");
        }
        if (command.amount() == null || command.amount().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 필수입니다.");
        }
        if (command.callbackUrl() == null || command.callbackUrl().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "콜백 URL은 필수입니다.");
        }
    }
    
    private static String maskCardNumber(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return cardNo;
        }
        return cardNo.substring(0, 4) + "-****-****-" + cardNo.substring(cardNo.length() - 4);
    }
    
    public void assignPaymentId(PaymentCommand.AssignPaymentId command) {
        if (this.paymentId != null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 ID는 이미 할당되었습니다.");
        }
        if (command.paymentId() == null || command.paymentId().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 ID는 필수입니다.");
        }
        this.paymentId = command.paymentId();
    }
    
    public void updateStatus(PaymentCommand.UpdateStatus command) {
        if (command.status() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태는 필수입니다.");
        }
        
        if (this.status == PaymentStatus.COMPLETED && command.status() != PaymentStatus.COMPLETED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "완료된 결제의 상태는 변경할 수 없습니다.");
        }
        if (this.status == PaymentStatus.FAILED && command.status() != PaymentStatus.FAILED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "실패한 결제의 상태는 변경할 수 없습니다.");
        }
        if (this.status == PaymentStatus.CANCELLED && command.status() != PaymentStatus.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "취소된 결제의 상태는 변경할 수 없습니다.");
        }
        
        this.status = command.status();
    }
    
    public void completePayment(PaymentCommand.Complete command) {
        if (command.transactionId() == null || command.transactionId().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "트랜잭션 ID는 필수입니다.");
        }
        
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = command.transactionId();
        this.processedAt = ZonedDateTime.now();
    }
    
    public void failPayment(PaymentCommand.Fail command) {
        if (command.errorMessage() == null || command.errorMessage().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "에러 메시지는 필수입니다.");
        }
        
        this.status = PaymentStatus.FAILED;
        this.errorMessage = command.errorMessage();
        this.processedAt = ZonedDateTime.now();
    }
    
    public void cancelPayment(PaymentCommand.Cancel command) {
        if (command.reason() == null || command.reason().trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "취소 사유는 필수입니다.");
        }
        
        this.status = PaymentStatus.CANCELLED;
        this.errorMessage = command.reason();
        this.processedAt = ZonedDateTime.now();
    }
    
    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(this.status);
    }
    
    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status);
    }
    
    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }
    
    public boolean isProcessing() {
        return PaymentStatus.PROCESSING.equals(this.status);
    }
    
    public boolean isCancelled() {
        return PaymentStatus.CANCELLED.equals(this.status);
    }
}
