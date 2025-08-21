package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {
    
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public PaymentEntity save(PaymentEntity payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentEntity> findByPaymentId(String paymentId) {
        return paymentJpaRepository.findByPaymentId(paymentId);
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(String orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<PaymentEntity> findByUserId(Long userId) {
        return paymentJpaRepository.findByUserId(userId);
    }

    @Override
    public List<PaymentEntity> findByStatus(PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status);
    }

    @Override
    public List<PaymentEntity> findPendingPayments() {
        return paymentJpaRepository.findPendingPayments();
    }

    @Override
    public void delete(PaymentEntity payment) {
        paymentJpaRepository.delete(payment);
    }
}
