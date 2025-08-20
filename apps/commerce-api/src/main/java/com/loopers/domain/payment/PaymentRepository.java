package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    PaymentEntity save(PaymentEntity payment);
    
    Optional<PaymentEntity> findByPaymentId(String paymentId);
    
    Optional<PaymentEntity> findByOrderId(String orderId);
    
    List<PaymentEntity> findByUserId(Long userId);
    
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    List<PaymentEntity> findPendingPayments();
    
    void delete(PaymentEntity payment);
}
