package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgV1Dto;
import com.loopers.interfaces.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PgPaymentGatewayImplIntegrationTest {

    @Mock
    private PgV1FeignClient pgV1FeignClient;

    @InjectMocks
    private PgPaymentGatewayImpl pgPaymentGateway;


    @Test
    @DisplayName("Mock 동작 확인 테스트")
    void Mock_동작_확인() {
        // given
        PgV1Dto.Response.Transaction transactionData = PgV1Dto.Response.Transaction.builder()
            .transactionKey("TEST:TR:12345")
            .orderId("test123")
            .cardType("SAMSUNG")
            .cardNo("1234-5678-9814-1451")
            .amount(1000L)
            .status("PENDING")
            .reason(null)
            .build();

        ApiResponse<PgV1Dto.Response.Transaction> mockResponse = ApiResponse.success(transactionData);

        when(pgV1FeignClient.request(eq("135135"), any(PgV1Dto.Request.Transaction.class)))
            .thenReturn(mockResponse);
        
        // when - 실제 결제 요청 테스트
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "test123", "SAMSUNG", "1234-5678-9814-1451", 
                "1000", "http://localhost:8080/callback"
            )
        );
        
        PaymentInfo paymentInfo = pgPaymentGateway.requestPayment(payment);
        
        // then
        assertThat(paymentInfo).isNotNull();
        assertThat(paymentInfo.transactionKey()).isEqualTo("TEST:TR:12345");
        assertThat(paymentInfo.status()).isEqualTo("PENDING");
        
        System.out.println("pgPaymentGateway: " + pgPaymentGateway);
        System.out.println("pgV1FeignClient: " + pgV1FeignClient);
        System.out.println("결제 요청 성공 후후 트랜잭션 키: " + paymentInfo.transactionKey());
    }

    @Test
    @DisplayName("Mock을 사용한 결제 요청 성공 테스트")
    void Mock_결제요청_성공() {
        // given
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "1351039135", "SAMSUNG", "1234-5678-9814-1451", 
                "5000", "http://localhost:8080/api/v1/examples/callback"
            )
        );

        // Mock 응답 설정
        PgV1Dto.Response.Transaction transactionData = PgV1Dto.Response.Transaction.builder()
            .transactionKey("20250816:TR:12345")
            .orderId("1351039135")
            .cardType("SAMSUNG")
            .cardNo("1234-5678-9814-1451")
            .amount(5000L)
            .status("PENDING")
            .reason(null)
            .build();

        ApiResponse<PgV1Dto.Response.Transaction> mockResponse = ApiResponse.success(transactionData);

        when(pgV1FeignClient.request(eq("135135"), any(PgV1Dto.Request.Transaction.class)))
            .thenReturn(mockResponse);
        
        // when
        PaymentInfo paymentInfo = pgPaymentGateway.requestPayment(payment);
        
        // then
        assertThat(paymentInfo).isNotNull();
        assertThat(paymentInfo.transactionKey()).isEqualTo("20250816:TR:12345");
        assertThat(paymentInfo.status()).isEqualTo("PENDING");
        assertThat(paymentInfo.orderId()).isEqualTo("1351039135");
    }

    @Test
    @DisplayName("Mock을 사용한 결제 요청 및 상태 조회 통합 테스트")
    void Mock_결제요청_및_상태조회_통합테스트() {
        // given
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "1351039136", "KB", "9876-5432-1098-7654", 
                "10000", "http://localhost:8080/callback"
            )
        );
        
        // Mock 응답 설정 - 결제 요청
        PgV1Dto.Response.Transaction paymentTransactionData = PgV1Dto.Response.Transaction.builder()
            .transactionKey("20250816:TR:98765")
            .orderId("1351039136")
            .cardType("KB")
            .cardNo("9876-5432-1098-7654")
            .amount(10000L)
            .status("approved")
            .reason(null)
            .build();

        ApiResponse<PgV1Dto.Response.Transaction> paymentResponse = ApiResponse.success(paymentTransactionData);

        when(pgV1FeignClient.request(eq("135135"), any(PgV1Dto.Request.Transaction.class)))
            .thenReturn(paymentResponse);
        
        PaymentInfo paymentInfo = pgPaymentGateway.requestPayment(payment);

        // Mock 응답 설정 - 결제 상태 조회
        PgV1Dto.Response.Transaction statusTransactionData = PgV1Dto.Response.Transaction.builder()
            .transactionKey("20250816:TR:98765")
            .orderId("1351039136")
            .cardType("KB")
            .cardNo("9876-5432-1098-7654")
            .amount(10000L)
            .status("approved")
            .reason(null)
            .build();

        ApiResponse<PgV1Dto.Response.Transaction> statusResponse = ApiResponse.success(statusTransactionData);

        when(pgV1FeignClient.findTransaction(eq("20250816:TR:98765"), eq("135135")))
            .thenReturn(statusResponse);
        
        // when
        PaymentInfo statusInfo = pgPaymentGateway.getPaymentTransactionDetail("20250816:TR:98765");
        
        // then
        assertThat(paymentInfo).isNotNull();
        assertThat(paymentInfo.transactionKey()).isEqualTo("20250816:TR:98765");
        assertThat(paymentInfo.status()).isEqualTo("approved");
        
        assertThat(statusInfo).isNotNull();
        assertThat(statusInfo.transactionKey()).isEqualTo("20250816:TR:98765");
        assertThat(statusInfo.status()).isEqualTo("approved");
        assertThat(statusInfo.orderId()).isEqualTo("1351039136");
        assertThat(statusInfo.amount()).isEqualTo(10000L);
        
        System.out.println("결제 요청 성공 후 트랜잭션 키: " + paymentInfo.transactionKey());
        System.out.println("상태 조회 성공 후 상태: " + statusInfo.status());
    }
}
