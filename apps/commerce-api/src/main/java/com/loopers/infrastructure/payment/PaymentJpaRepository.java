package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
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
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status = 'PENDING' AND p.createdAt < :threshold")
    List<PaymentEntity> findPendingPaymentsOlderThan(@Param("threshold") ZonedDateTime threshold);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.status IN ('COMPLETED', 'FAILED', 'CANCELLED') AND p.createdAt < :threshold")
    List<PaymentEntity> findOldCompletedPayments(@Param("threshold") ZonedDateTime threshold);
}
