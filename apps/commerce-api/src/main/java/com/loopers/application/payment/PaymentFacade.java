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
}
