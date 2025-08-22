package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentGatewayImpl implements PaymentGateway {

    private final PgV1FeignClient pgV1FeignClient;

    @Override
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

    @Override
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
}
