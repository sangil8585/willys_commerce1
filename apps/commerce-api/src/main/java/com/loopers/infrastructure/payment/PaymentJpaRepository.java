package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    
    Optional<PaymentEntity> findByPaymentId(String paymentId);
    
    Optional<PaymentEntity> findByOrderId(String orderId);
    
    List<PaymentEntity> findByUserId(Long userId);
    
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    List<PaymentEntity> findByStatusIn(List<PaymentStatus> statuses);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = 'PENDING'")
    List<PaymentEntity> findPendingPayments();
}
