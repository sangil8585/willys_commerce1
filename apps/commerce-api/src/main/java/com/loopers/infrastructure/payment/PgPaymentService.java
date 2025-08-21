package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionDetailResponse;

public interface PgPaymentService {
    
    /**
     * PG 결제 요청
     * @param request 결제 요청 정보
     * @return 결제 응답 정보
     */
    PgPaymentResponse requestPayment(PgPaymentRequest request);
    
    /**
     * PG 트랜잭션 상세 조회
     * @param transactionKey 트랜잭션 키
     * @return 트랜잭션 상세 정보
     */
    PgTransactionDetailResponse getTransactionDetail(String transactionKey);
}
