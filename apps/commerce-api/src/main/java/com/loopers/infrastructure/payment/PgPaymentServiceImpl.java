package com.loopers.infrastructure.payment;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentServiceImpl implements PgPaymentService {

    private final RestTemplate pgPaymentRestTemplate;
    
    @Value("${pg.base-url:http://localhost:8081}")
    private String pgBaseUrl;
    
    @Value("${pg.default-user-id:135135}")
    private String defaultUserId;

    @Override
    public PgPaymentResponse requestPayment(PgPaymentRequest request) {
        try {
            String url = pgBaseUrl + "/api/v1/payments";
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Id", defaultUserId);
            
            HttpEntity<PgPaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PgPaymentResponse> response = pgPaymentRestTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                PgPaymentResponse.class
            );
            
            return response.getBody();
            
        } catch (ResourceAccessException e) {
            log.error("PG 시스템 연결 실패: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 시스템 연결에 실패했습니다.");
        } catch (Exception e) {
            log.error("PG 결제 요청 중 오류 발생: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 결제 요청 중 오류가 발생했습니다.");
        }
    }

    @Override
    public PgTransactionDetailResponse getTransactionDetail(String transactionKey) {
        try {
            String url = pgBaseUrl + "/api/v1/payments/" + transactionKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Id", defaultUserId);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<PgTransactionDetailResponse> response = pgPaymentRestTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                PgTransactionDetailResponse.class
            );
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("PG 트랜잭션 상세 조회 중 오류 발생: {}", e.getMessage());
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 트랜잭션 상세 조회 중 오류가 발생했습니다.");
        }
    }
}
