package com.loopers.interfaces.api.admin;

import com.loopers.application.payment.PaymentRecoveryScheduler;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentRecoveryScheduler paymentRecoveryScheduler;

    /**
     * 특정 주문의 결제 상태를 수동으로 복구합니다.
     * 콜백이 오지 않았거나 배치 작업에서 놓친 경우 사용할 수 있습니다.
     */
    @PostMapping("/{orderId}/recovery")
    public ApiResponse<Object> recoverPaymentStatus(@PathVariable String orderId) {
        try {
            log.info("관리자 결제 상태 복구 요청: orderId={}", orderId);
            
            paymentRecoveryScheduler.manualRecovery(orderId);
            
            return ApiResponse.success("결제 상태 복구가 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("관리자 결제 상태 복구 실패: orderId={}, error={}", orderId, e.getMessage());
            return ApiResponse.fail("PAYMENT_RECOVERY_FAILED", "결제 상태 복구에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/batch/execute")
    public ApiResponse<Object> executeBatchImmediately() {
        try {
            log.info("관리자 배치 작업 즉시 실행 요청");
            
            // PENDING 상태의 결제 건들을 즉시 복구
            paymentRecoveryScheduler.recoverPendingPayments();
            
            return ApiResponse.success("배치 작업이 즉시 실행되었습니다.");
            
        } catch (Exception e) {
            log.error("관리자 배치 작업 즉시 실행 실패: error={}", e.getMessage());
            return ApiResponse.fail("BATCH_EXECUTION_FAILED", "배치 작업 실행에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/batch/cleanup")
    public ApiResponse<Object> executeCleanupImmediately() {
        try {
            log.info("관리자 오래된 결제 건 정리 요청");
            
            // 오래된 결제 건들을 즉시 정리
            paymentRecoveryScheduler.cleanupOldPayments();
            
            return ApiResponse.success("오래된 결제 건 정리가 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("관리자 오래된 결제 건 정리 실패: error={}", e.getMessage());
            return ApiResponse.fail("CLEANUP_FAILED", "오래된 결제 건 정리에 실패했습니다: " + e.getMessage());
        }
    }
}
