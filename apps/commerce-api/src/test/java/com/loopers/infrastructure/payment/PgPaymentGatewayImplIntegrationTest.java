package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PgPaymentGatewayImplIntegrationTest {

    @Mock
    private RestTemplate pgPaymentGatewayRestTemplate;

    @InjectMocks
    private PgPaymentGatewayImpl pgPaymentGateway;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pgPaymentGateway, "pgBaseUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(pgPaymentGateway, "defaultUserId", "135135");
        
        ReflectionTestUtils.setField(pgPaymentGateway, "pgPaymentGatewayRestTemplate", pgPaymentGatewayRestTemplate);
    }

    @Test
    @DisplayName("Mock 동작 확인 테스트")
    void Mock_동작_확인() {
        // given
        PgPaymentResponse mockResponse = PgPaymentResponse.builder()
            .transactionKey("TEST:TR:12345")
            .status("PENDING")
            .reason(null)
            .build();

        when(pgPaymentGatewayRestTemplate.exchange(
            eq("http://localhost:8082/api/v1/payments"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(PgPaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));
        
        // when - 실제 결제 요청 테스트
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "test123", "SAMSUNG", "1234-5678-9814-1451", 
                "1000", "http://localhost:8080/callback"
            )
        );
        
        PaymentInfo paymentInfo = pgPaymentGateway.requestPayment(payment);
        
        // then - Mock이 작동하면 정상적으로 응답을 받아야 함
        assertThat(paymentInfo).isNotNull();
        assertThat(paymentInfo.transactionKey()).isEqualTo("TEST:TR:12345");
        assertThat(paymentInfo.status()).isEqualTo("PENDING");
        
        System.out.println("pgPaymentGateway: " + pgPaymentGateway);
        System.out.println("pgPaymentGatewayRestTemplate: " + pgPaymentGatewayRestTemplate);
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
        PgPaymentResponse mockResponse = PgPaymentResponse.builder()
            .transactionKey("20250816:TR:12345")
            .status("PENDING")
            .reason(null)
            .build();

        when(pgPaymentGatewayRestTemplate.exchange(
            eq("http://localhost:8082/api/v1/payments"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(PgPaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // when
        PaymentInfo paymentInfo = pgPaymentGateway.requestPayment(payment);

        // then
        assertThat(paymentInfo).isNotNull();
        assertThat(paymentInfo.transactionKey()).isEqualTo("20250816:TR:12345");
        assertThat(paymentInfo.status()).isEqualTo("PENDING");
        
        System.out.println("Mock 결제 요청 성공!");
        System.out.println("트랜잭션 키: " + paymentInfo.transactionKey());
    }

    @Test
    @DisplayName("MOCK으로 결제 상태 조회")
    void 실제_결제상태_조회() {
        // given - 먼저 결제 요청
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "1351039136", "KB", "9876-5432-1098-7654", 
                "10000", "http://localhost:8080/api/v1/examples/callback"
            )
        );
        
        // Mock 응답 설정 - 결제 요청
        PgPaymentResponse paymentResponse = PgPaymentResponse.builder()
            .transactionKey("20250816:TR:98765")
            .status("PENDING")
            .reason(null)
            .build();

        when(pgPaymentGatewayRestTemplate.exchange(
            eq("http://localhost:8082/api/v1/payments"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(PgPaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(paymentResponse, HttpStatus.OK));
        
        PaymentInfo paymentInfo = pgPaymentGateway.requestPayment(payment);

        // Mock 응답 설정 - 결제 상태 조회
        PgTransactionDetailResponse statusResponse = PgTransactionDetailResponse.builder()
            .transactionKey("20250816:TR:98765")
            .orderId("1351039136")
            .cardType("KB")
            .cardNo("9876-5432-1098-7654")
            .amount(10000L)
            .status("approved")
            .reason(null)
            .build();

        when(pgPaymentGatewayRestTemplate.exchange(
            eq("http://localhost:8082/api/v1/payments/" + paymentInfo.transactionKey()),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PgTransactionDetailResponse.class)
        )).thenReturn(new ResponseEntity<>(statusResponse, HttpStatus.OK));

        // when - 결제 상태 조회
        PaymentInfo transactionDetail = pgPaymentGateway.getPaymentTransactionDetail(paymentInfo.transactionKey());

        // then
        assertThat(transactionDetail).isNotNull();
        assertThat(transactionDetail.transactionKey()).isEqualTo("20250816:TR:98765");
        assertThat(transactionDetail.status()).isEqualTo("approved");
        
        System.out.println("Mock 결제 상태 조회 성공!");
        System.out.println("트랜잭션 키: " + paymentInfo.transactionKey());
        System.out.println("상태: " + transactionDetail.status());
    }
}
