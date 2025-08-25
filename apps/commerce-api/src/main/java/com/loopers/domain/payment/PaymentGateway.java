package com.loopers.domain.payment;

/**
 * 결제 게이트웨이 인터페이스
 * 외부 PG 시스템과의 결제 연동을 담당하는 포트
 */
public interface PaymentGateway {
    
    /**
     * PG 결제 요청
     * @param payment 결제 엔티티
     * @return 결제 정보
     */
    PaymentInfo requestPayment(PaymentEntity payment);
    
    /**
     * PG 트랜잭션 상세 조회
     * @param transactionKey 트랜잭션 키
     * @return 결제 정보
     */
    PaymentInfo getPaymentTransactionDetail(String transactionKey);
}
