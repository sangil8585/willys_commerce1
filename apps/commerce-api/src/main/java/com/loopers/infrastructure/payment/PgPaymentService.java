package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.dto.PgV1Dto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.ErrorType;
import com.loopers.support.error.PgPaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * PG 결제 서비스
 * Resilience4j가 적용된 FeignClient를 사용하여 외부 PG 시스템과 통신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentService {

    private final PgV1FeignClient pgV1FeignClient;

    /**
     * 결제 요청
     * @param userId 사용자 ID
     * @param request 결제 요청 정보
     * @return 결제 응답
     */
    public ApiResponse<PgV1Dto.Response.Transaction> requestPayment(String userId, PgV1Dto.Request.Transaction request) {
        try {
            log.info("PG 결제 요청 시작 - userId: {}, orderId: {}, amount: {}", 
                userId, request.orderId(), request.amount());
            
            ApiResponse<PgV1Dto.Response.Transaction> response = pgV1FeignClient.request(userId, request);
            
            log.info("PG 결제 요청 성공 - userId: {}, orderId: {}, status: {}", 
                userId, request.orderId(), response.data().status());
            
            return response;
            
        } catch (Exception e) {
            log.error("PG 결제 요청 실패 - userId: {}, orderId: {}, error: {}", 
                userId, request.orderId(), e.getMessage(), e);
            
            // Resilience4j에 의해 발생하는 예외들을 적절한 PgPaymentException으로 변환
            if (e instanceof java.util.concurrent.TimeoutException) {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_TIMEOUT, "PG 결제 요청이 타임아웃되었습니다.", null, null, e);
            } else if (e instanceof java.io.IOException) {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_NETWORK_ERROR, "PG 네트워크 연결에 실패했습니다.", null, null, e);
            } else {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_SYSTEM_ERROR, "PG 결제 처리 중 오류가 발생했습니다.", null, null, e);
            }
        }
    }

    /**
     * 주문 상태 조회
     * @param userId 사용자 ID
     * @param orderId 주문 ID
     * @return 주문 상태
     */
    public ApiResponse<PgV1Dto.Response.Order> findOrderStatus(String userId, String orderId) {
        try {
            log.info("PG 주문 상태 조회 시작 - userId: {}, orderId: {}", userId, orderId);
            
            ApiResponse<PgV1Dto.Response.Order> response = pgV1FeignClient.findOrder(orderId, userId);
            
            log.info("PG 주문 상태 조회 성공 - userId: {}, orderId: {}, status: {}", 
                userId, orderId, response.data().status());
            
            return response;
            
        } catch (Exception e) {
            log.error("PG 주문 상태 조회 실패 - userId: {}, orderId: {}, error: {}", 
                userId, orderId, e.getMessage(), e);
            
            if (e instanceof java.util.concurrent.TimeoutException) {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_TIMEOUT, "PG 주문 상태 조회가 타임아웃되었습니다.", null, null, e);
            } else if (e instanceof java.io.IOException) {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_NETWORK_ERROR, "PG 네트워크 연결에 실패했습니다.", null, null, e);
            } else {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_SYSTEM_ERROR, "PG 주문 상태 조회 중 오류가 발생했습니다.", null, null, e);
            }
        }
    }

    /**
     * 거래 상세 조회
     * @param userId 사용자 ID
     * @param transactionKey 거래 키
     * @return 거래 상세 정보
     */
    public ApiResponse<PgV1Dto.Response.Transaction> findTransactionDetail(String userId, String transactionKey) {
        try {
            log.info("PG 거래 상세 조회 시작 - userId: {}, transactionKey: {}", userId, transactionKey);
            
            ApiResponse<PgV1Dto.Response.Transaction> response = pgV1FeignClient.findTransaction(transactionKey, userId);
            
            log.info("PG 거래 상세 조회 성공 - userId: {}, transactionKey: {}, status: {}", 
                userId, transactionKey, response.data().status());
            
            return response;
            
        } catch (Exception e) {
            log.error("PG 거래 상세 조회 실패 - userId: {}, transactionKey: {}, error: {}", 
                userId, transactionKey, e.getMessage(), e);
            
            if (e instanceof java.util.concurrent.TimeoutException) {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_TIMEOUT, "PG 거래 상세 조회가 타임아웃되었습니다.", null, null, e);
            } else if (e instanceof java.io.IOException) {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_NETWORK_ERROR, "PG 네트워크 연결에 실패했습니다.", null, null, e);
            } else {
                throw new PgPaymentException(ErrorType.PG_PAYMENT_SYSTEM_ERROR, "PG 거래 상세 조회 중 오류가 발생했습니다.", null, null, e);
            }
        }
    }
}
