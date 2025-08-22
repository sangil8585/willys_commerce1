package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionDetailResponse;
import com.loopers.support.error.CoreException;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import com.loopers.domain.payment.PaymentCommand;

@ExtendWith(MockitoExtension.class)
class PgPaymentGatewayImplTest {

    @Mock
    private RestTemplate pgPaymentGatewayRestTemplate;

    @InjectMocks
    private PgPaymentGatewayImpl pgPaymentGateway;

    @BeforeEach
    void setUp() {
        // Mock RestTemplate 설정
        ReflectionTestUtils.setField(pgPaymentGateway, "pgBaseUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(pgPaymentGateway, "defaultUserId", "135135");
        
        // Mock RestTemplate 직접 주입
        ReflectionTestUtils.setField(pgPaymentGateway, "pgPaymentGatewayRestTemplate", pgPaymentGatewayRestTemplate);
    }

    @Test
    @DisplayName("PG 결제 요청 성공")
    void pg_결제요청_성공() {
        // given
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "1351039135", "SAMSUNG", "1234-5678-9814-1451", 
                "5000", "http://localhost:8080/api/v1/examples/callback"
            )
        );

        PgPaymentResponse expectedResponse = PgPaymentResponse.builder()
            .transactionKey("20250816:TR:9577c5")
            .status("PENDING")
            .reason(null)
            .build();

        when(pgPaymentGatewayRestTemplate.exchange(
            eq("http://localhost:8082/api/v1/payments"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(PgPaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        PaymentInfo result = pgPaymentGateway.requestPayment(payment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.transactionKey()).isEqualTo("20250816:TR:9577c5");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.orderId()).isEqualTo("1351039135");
    }

    @Test
    @DisplayName("PG 결제 요청 중 연결 오류 발생")
    void pg_결제요청시_연결오류발생() {
        // given
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "1351039135", "SAMSUNG", "1234-5678-9814-1451", 
                "5000", "http://localhost:8080/api/v1/examples/callback"
            )
        );

        when(pgPaymentGatewayRestTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(PgPaymentResponse.class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        // when & then
        assertThatThrownBy(() -> pgPaymentGateway.requestPayment(payment))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("PG 시스템 연결에 실패했습니다.");
    }

    @Test
    @DisplayName("PG 트랜잭션 상세 조회 성공")
    void getPaymentTransactionDetail_Success() {
        // given
        String transactionKey = "20250816:TR:9577c5";
        
        PgTransactionDetailResponse expectedResponse = PgTransactionDetailResponse.builder()
            .transactionKey("20250816:TR:9577c5")
            .orderId("1351039135")
            .cardType("SAMSUNG")
            .cardNo("1234-5678-9814-1451")
            .amount(5000L)
            .status("approved")
            .reason(null)
            .build();

        when(pgPaymentGatewayRestTemplate.exchange(
            eq("http://localhost:8082/api/v1/payments/" + transactionKey),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PgTransactionDetailResponse.class)
        )).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        PaymentInfo result = pgPaymentGateway.getPaymentTransactionDetail(transactionKey);

        // then
        assertThat(result).isNotNull();
        assertThat(result.transactionKey()).isEqualTo("20250816:TR:9577c5");
        assertThat(result.status()).isEqualTo("approved");
        assertThat(result.orderId()).isEqualTo("1351039135");
        assertThat(result.amount()).isEqualTo(5000L);
    }
}
