package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.payment.PgPaymentGatewayImpl;
import com.loopers.domain.payment.PaymentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRecoveryScheduler {

    private final PaymentRepository paymentRepository;
    private final PgPaymentGatewayImpl pgPaymentGateway;

    /**
     * 5분마다 PENDING 상태의 결제 건들을 조회하여 상태를 복구합니다.
     * 콜백이 오지 않아도 주기적으로 상태를 확인할 수 있습니다.
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    @Transactional
    public void recoverPendingPayments() {
        log.info("결제 상태 복구 배치 작업 시작");
        
        try {
            // PENDING 상태의 결제 건들 조회 (30분 이상 대기 중인 건들)
            ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(30);
            List<PaymentEntity> pendingPayments = paymentRepository.findPendingPaymentsOlderThan(threshold);
            
            if (pendingPayments.isEmpty()) {
                log.info("복구할 PENDING 결제 건이 없습니다.");
                return;
            }
            
            log.info("복구 대상 결제 건 수: {}", pendingPayments.size());
            
            int recoveredCount = 0;
            int failedCount = 0;
            
            for (PaymentEntity payment : pendingPayments) {
                try {
                    // PG 시스템에 현재 상태 재확인
                    PaymentInfo paymentInfo = pgPaymentGateway.getPaymentTransactionDetail(payment.getTransactionId());
                    
                    if (paymentInfo != null) {
                        // 상태 업데이트
                        updatePaymentStatus(payment, paymentInfo);
                        recoveredCount++;
                        log.info("결제 상태 복구 성공: orderId={}, status={}", 
                                payment.getOrderId(), paymentInfo.status());
                    }
                    
                } catch (Exception e) {
                    failedCount++;
                    log.error("결제 상태 복구 실패: orderId={}, error={}", 
                            payment.getOrderId(), e.getMessage());
                    
                    // 실패한 건은 TIMEOUT 상태로 변경
                    markPaymentAsTimeout(payment);
                }
            }
            
            log.info("결제 상태 복구 배치 작업 완료: 성공={}, 실패={}", recoveredCount, failedCount);
            
        } catch (Exception e) {
            log.error("결제 상태 복구 배치 작업 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 매일 새벽 2시에 오래된 결제 건들을 정리합니다.
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    @Transactional
    public void cleanupOldPayments() {
        log.info("오래된 결제 건 정리 배치 작업 시작");
        
        try {
            // 7일 이상 된 완료/실패/취소된 결제 건들 조회
            ZonedDateTime threshold = ZonedDateTime.now().minusDays(7);
            List<PaymentEntity> oldPayments = paymentRepository.findOldCompletedPayments(threshold);
            
            if (oldPayments.isEmpty()) {
                log.info("정리할 오래된 결제 건이 없습니다.");
                return;
            }
            
            log.info("정리 대상 결제 건 수: {}", oldPayments.size());
            
            // 오래된 결제 건들을 ARCHIVED 상태로 변경
            for (PaymentEntity payment : oldPayments) {
                payment.updateStatus(new com.loopers.domain.payment.PaymentCommand.UpdateStatus(PaymentStatus.ARCHIVED));
            }
            
            log.info("오래된 결제 건 정리 완료: {}건", oldPayments.size());
            
        } catch (Exception e) {
            log.error("오래된 결제 건 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 수동으로 결제 상태 복구를 실행할 수 있는 메서드
     */
    @Transactional
    public void manualRecovery(String orderId) {
        log.info("수동 결제 상태 복구 시작: orderId={}", orderId);
        
        try {
            PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
            
            if (payment.getStatus() != PaymentStatus.PENDING) {
                log.warn("PENDING 상태가 아닌 결제 건입니다: orderId={}, status={}", 
                        orderId, payment.getStatus());
                return;
            }
            
            // PG 시스템에 상태 재확인
            PaymentInfo paymentInfo = pgPaymentGateway.getPaymentTransactionDetail(payment.getTransactionId());
            
            if (paymentInfo != null) {
                updatePaymentStatus(payment, paymentInfo);
                log.info("수동 결제 상태 복구 성공: orderId={}, status={}", 
                        orderId, paymentInfo.status());
            }
            
        } catch (Exception e) {
            log.error("수동 결제 상태 복구 실패: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("결제 상태 복구에 실패했습니다: " + e.getMessage());
        }
    }

    private void updatePaymentStatus(PaymentEntity payment, PaymentInfo paymentInfo) {
        // PG 응답에 따라 상태 업데이트
        if ("SUCCESS".equals(paymentInfo.status())) {
            payment.completePayment(new com.loopers.domain.payment.PaymentCommand.Complete(paymentInfo.transactionKey()));
        } else if ("FAILED".equals(paymentInfo.status())) {
            payment.failPayment(new com.loopers.domain.payment.PaymentCommand.Fail(paymentInfo.reason()));
        }
        // PENDING 상태는 그대로 유지
    }

    private void markPaymentAsTimeout(PaymentEntity payment) {
        payment.failPayment(new com.loopers.domain.payment.PaymentCommand.Fail("PG 시스템 응답 타임아웃"));
    }
}
