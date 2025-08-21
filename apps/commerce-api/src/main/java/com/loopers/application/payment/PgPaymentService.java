package com.loopers.application.payment;

import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionDetailResponse;

import java.util.List;

public interface PgPaymentService {
    
    /**
     * PG 결제 요청
     * @param request 결제 요청 정보
     * @return 결제 응답 정보
     */
    PgPaymentResponse requestPayment(PgPaymentRequest request);
    
    /**
     * 트랜잭션 키로 결제 정보 조회
     * @param transactionKey 트랜잭션 키
     * @return 결제 상세 정보
     */
    PgTransactionDetailResponse getTransactionDetail(String transactionKey);
    
    /**
     * 주문 ID로 결제 정보 조회
     * @param orderId 주문 ID
     * @return 주문별 결제 정보 목록
     */
    List<PgTransactionDetailResponse> getTransactionsByOrderId(String orderId);
}
