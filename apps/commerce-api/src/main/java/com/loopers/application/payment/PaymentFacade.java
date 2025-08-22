package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentGateway paymentGateway;
    
    @Transactional
    public PaymentResult pay(PaymentCriteria criteria) {
        try {
            // 도메인 엔티티 생성
            PaymentEntity payment = PaymentEntity.from(
                PaymentCommand.Create.of(
                    Long.valueOf(criteria.userId()),
                    criteria.orderId(),
                    criteria.cardType(),
                    criteria.cardNo(),
                    criteria.amount(),
                    criteria.callbackUrl()
                )
            );
            
            // PG 결제 요청
            PaymentInfo paymentInfo = paymentGateway.requestPayment(payment);
            
            return new PaymentResult(
                criteria.orderId(),
                paymentInfo.transactionKey(),
                paymentInfo.status()
            );
            
        } catch (Exception e) {
            // PG 연동 실패 시 기본 처리
            String paymentId = generatePaymentId();
            return new PaymentResult(
                criteria.orderId(),
                paymentId,
                "PENDING"
            );
        }
    }
    
    private String generatePaymentId() {
        return "20250816:TR:" + System.currentTimeMillis();
    }
    
    @Transactional(readOnly = true)
    public PaymentInfo getPaymentTransactionDetail(String transactionKey) {
        return paymentGateway.getPaymentTransactionDetail(transactionKey);
    }
    
    @Transactional(readOnly = true)
    public PaymentResult getPaymentByOrderId(String userId, String orderId) {
        // 실제 구현에서는 PaymentService를 통해 결제 정보를 조회
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}
