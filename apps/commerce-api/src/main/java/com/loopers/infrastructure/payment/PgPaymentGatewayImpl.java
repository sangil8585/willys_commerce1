package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentGatewayImpl implements PaymentGateway {

    private final PgV1FeignClient pgV1FeignClient;

    @Override
    @Retry(name = "pgRetry", fallbackMethod = "requestPaymentFallback")
    @CircuitBreaker(name = "pgCircuitBreaker", fallbackMethod = "requestPaymentFallback")
    @TimeLimiter(name = "pgTimeLimiter", fallbackMethod = "requestPaymentFallback")
    public PaymentInfo requestPayment(PaymentEntity payment) {
        try {
            log.info("PG 결제 요청 시작: orderId={}", payment.getOrderId());
            
            PgV1Dto.Request.Transaction request = PgV1Dto.Request.Transaction.of(
                payment.getOrderId(),
                payment.getCardType(),
                payment.getCardNo(),
                String.valueOf(payment.getAmount()),
                payment.getCallbackUrl()
            );
            
            var response = pgV1FeignClient.request("135135", request);
            
            if (response == null || response.data() == null) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 응답이 null입니다.");
            }
            
            return PaymentInfo.of(
                response.data().transactionKey(),
                payment.getOrderId(),
                payment.getCardType(),
                payment.getCardNo(),
                Long.parseLong(payment.getAmount()),
                response.data().status(),
                response.data().reason()
            );
            
        } catch (Exception e) {
            log.error("PG 결제 요청 중 오류 발생: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 결제 요청 중 오류가 발생했습니다.");
        }
    }

    /**
     * 결제 요청 실패 시 fallback 메서드
     * 재시도가 모두 실패하거나 서킷 브레이커가 열린 상태일 때 호출됨
     */
    public PaymentInfo requestPaymentFallback(PaymentEntity payment, Throwable throwable) {
        log.warn("결제 요청 fallback 실행: orderId={}, error={}", payment.getOrderId(), throwable.getMessage());
        
        // 장애 상황에서도 내부 시스템은 정상적으로 응답하도록 보호
        return PaymentInfo.of(
                generateTransactionKey(payment.getOrderId()),
                payment.getOrderId(),
                payment.getCardType(),
                payment.getCardNo(),
                Long.parseLong(payment.getAmount()),
                "PENDING", // 결제 대기 상태로 설정
                "PG 시스템 장애로 인한 대기 상태"
        );
    }

    @Override
    @Retry(name = "pgRetry", fallbackMethod = "getPaymentTransactionDetailFallback")
    @CircuitBreaker(name = "pgCircuitBreaker", fallbackMethod = "getPaymentTransactionDetailFallback")
    @TimeLimiter(name = "pgTimeLimiter", fallbackMethod = "getPaymentTransactionDetailFallback")
    public PaymentInfo getPaymentTransactionDetail(String transactionKey) {
        try {
            log.info("PG 트랜잭션 상세 조회: transactionKey={}", transactionKey);
            
            var response = pgV1FeignClient.findTransaction(transactionKey, "135135");
            
            if (response == null || response.data() == null) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 트랜잭션 상세 응답이 null입니다.");
            }
            
            var pgResponse = response.data();
            return PaymentInfo.of(
                pgResponse.transactionKey(),
                pgResponse.orderId(),
                pgResponse.cardType(),
                pgResponse.cardNo(),
                pgResponse.amount(),
                pgResponse.status(),
                pgResponse.reason()
            );
            
        } catch (Exception e) {
            log.error("PG 트랜잭션 상세 조회 중 오류 발생: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 트랜잭션 상세 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 트랜잭션 상세 조회 실패 시 fallback 메서드
     */
    public PaymentInfo getPaymentTransactionDetailFallback(String transactionKey, Throwable throwable) {
        log.warn("트랜잭션 상세 조회 fallback 실행: transactionKey={}, error={}", transactionKey, throwable.getMessage());
        
        // 장애 상황에서도 기본 정보라도 반환하여 시스템 안정성 확보
        return PaymentInfo.of(
                transactionKey,
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                0L,
                "PENDING",
                "PG 시스템 장애로 인한 대기 상태"
        );
    }

    /**
     * 트랜잭션 키 생성 헬퍼 메서드
     */
    private String generateTransactionKey(String orderId) {
        return "TR:" + orderId + ":" + System.currentTimeMillis();
    }
}
