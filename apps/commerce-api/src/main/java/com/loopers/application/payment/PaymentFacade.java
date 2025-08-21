package com.loopers.application.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFacade {
    
    @Transactional
    public PaymentResult pay(PaymentCriteria criteria) {
        String paymentId = generatePaymentId();
        
        return new PaymentResult(
            criteria.orderId(),
            paymentId,
            "PENDING"
        );
    }
    
    private String generatePaymentId() {
        return "20250816:TR:" + System.currentTimeMillis();
    }
    
    @Transactional(readOnly = true)
    public PaymentResult getPaymentByOrderId(String userId, String orderId) {
        // TODO: 실제 구현에서는 PaymentService를 통해 결제 정보를 조회
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}
