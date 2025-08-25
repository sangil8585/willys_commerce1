package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    @Transactional
    public PaymentEntity createPayment(PaymentCommand.Create command) {
        // 중복 주문 체크
        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderId(command.orderId());
        if (existingPayment.isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 결제가 진행 중인 주문입니다: " + command.orderId());
        }
        
        PaymentEntity payment = PaymentEntity.from(command);
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public void assignPaymentId(PaymentEntity payment, PaymentCommand.AssignPaymentId command) {
        payment.assignPaymentId(command);
        paymentRepository.save(payment);
    }
    
    @Transactional
    public void updatePaymentStatus(PaymentEntity payment, PaymentCommand.UpdateStatus command) {
        payment.updateStatus(command);
        paymentRepository.save(payment);
    }
    
    @Transactional
    public void completePayment(PaymentEntity payment, PaymentCommand.Complete command) {
        payment.completePayment(command);
        paymentRepository.save(payment);
    }
    
    @Transactional
    public void failPayment(PaymentEntity payment, PaymentCommand.Fail command) {
        payment.failPayment(command);
        paymentRepository.save(payment);
    }
    
    @Transactional
    public void cancelPayment(PaymentEntity payment, PaymentCommand.Cancel command) {
        payment.cancelPayment(command);
        paymentRepository.save(payment);
    }
    
    public PaymentEntity findByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다: " + paymentId));
    }
    
    public PaymentEntity findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문에 대한 결제 정보를 찾을 수 없습니다: " + orderId));
    }
    
    public List<PaymentEntity> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    public List<PaymentEntity> findPendingPayments() {
        return paymentRepository.findPendingPayments();
    }
    
    @Transactional
    public void deletePayment(PaymentEntity payment) {
        paymentRepository.delete(payment);
    }
}
