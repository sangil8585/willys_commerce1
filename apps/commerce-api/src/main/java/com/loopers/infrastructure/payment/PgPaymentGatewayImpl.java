package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionDetailResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentGatewayImpl implements PaymentGateway {

    private final RestTemplate pgPaymentGatewayRestTemplate;
    
    @Value("${pg-simulator.base-url:http://localhost:8081}")
    private String pgBaseUrl;
    
    @Value("${pg-simulator.user-id:135135}")
    private String defaultUserId;

    @Override
    public PaymentInfo requestPayment(PaymentEntity payment) {
        try {
            String url = pgBaseUrl + "/api/v1/payments";
            
            // Infrastructure DTO로 변환
            PgPaymentRequest request = PgPaymentRequest.of(
                payment.getOrderId(),
                payment.getCardType(),
                payment.getCardNo(),
                String.valueOf(payment.getAmount()),
                payment.getCallbackUrl()
            );
            
            log.info("PG 결제 요청 시작: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Id", defaultUserId);
            
            HttpEntity<PgPaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PgPaymentResponse> response = pgPaymentGatewayRestTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                PgPaymentResponse.class
            );
            
            if (response.getBody() == null) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 응답 바디가 null입니다.");
            }
            
            // Infrastructure DTO를 도메인 모델로 변환
            return PaymentInfo.of(
                response.getBody().transactionKey(),
                payment.getOrderId(),
                payment.getCardType(),
                payment.getCardNo(),
                Long.parseLong(payment.getAmount()),
                response.getBody().status(),
                response.getBody().reason()
            );
            
        } catch (ResourceAccessException e) {
            log.error("PG 시스템 연결 실패: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 시스템 연결에 실패했습니다.");
        } catch (Exception e) {
            log.error("PG 결제 요청 중 오류 발생: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 결제 요청 중 오류가 발생했습니다.");
        }
    }

    @Override
    public PaymentInfo getPaymentTransactionDetail(String transactionKey) {
        try {
            String url = pgBaseUrl + "/api/v1/payments/" + transactionKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Id", defaultUserId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<PgTransactionDetailResponse> response = pgPaymentGatewayRestTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                PgTransactionDetailResponse.class
            );
            
            if (response.getBody() == null) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 트랜잭션 상세 응답이 null입니다.");
            }
            
            // Infrastructure DTO를 도메인 모델로 변환
            PgTransactionDetailResponse pgResponse = response.getBody();
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
