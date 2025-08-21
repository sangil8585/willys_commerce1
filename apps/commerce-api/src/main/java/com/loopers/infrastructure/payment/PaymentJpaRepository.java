package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByPaymentId(String paymentId);

    Optional<PaymentEntity> findByOrderId(String orderId);

    List<PaymentEntity> findByUserId(Long userId);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM PaymentEntity p WHERE p.status = 'PENDING'")
    List<PaymentEntity> findPendingPayments();
}
