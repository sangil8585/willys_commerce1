package com.loopers.application.payment;

import com.loopers.infrastructure.payment.PgPaymentService;
import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFacade {
    
    private final PgPaymentService pgPaymentService;
    
    @Transactional
    public PaymentResult pay(PaymentCriteria criteria) {
        try {
            // PG 결제 요청
            PgPaymentRequest pgRequest = PgPaymentRequest.of(
                criteria.orderId(),
                criteria.cardType(),
                criteria.cardNo(),
                criteria.amount(),
                criteria.callbackUrl()
            );
            
            var pgResponse = pgPaymentService.requestPayment(pgRequest);
            
            return new PaymentResult(
                criteria.orderId(),
                pgResponse.transactionKey(),
                pgResponse.status()
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
}
