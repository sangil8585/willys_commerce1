package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.infrastructure.payment.dto.PgV1Dto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import com.loopers.domain.payment.PaymentCommand;

@ExtendWith(MockitoExtension.class)
class PgPaymentTest {

    @Mock
    private PgV1FeignClient pgV1FeignClient;

    @InjectMocks
    private PgPaymentGatewayImpl pgPaymentGateway;

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

        PgV1Dto.Response.Transaction transactionData = PgV1Dto.Response.Transaction.builder()
            .transactionKey("20250816:TR:9577c5")
            .orderId("1351039135")
            .cardType("SAMSUNG")
            .cardNo("1234-5678-9814-1451")
            .amount(5000L)
            .status("PENDING")
            .reason(null)
            .build();

        ApiResponse<PgV1Dto.Response.Transaction> expectedResponse = ApiResponse.success(transactionData);

        when(pgV1FeignClient.request(eq("135135"), any(PgV1Dto.Request.Transaction.class)))
            .thenReturn(expectedResponse);

        // when
        PaymentInfo result = pgPaymentGateway.requestPayment(payment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.transactionKey()).isEqualTo("20250816:TR:9577c5");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.orderId()).isEqualTo("1351039135");
    }

    @Test
    @DisplayName("PG 결제 요청 중 오류 발생")
    void pg_결제요청시_오류발생() {
        // given
        PaymentEntity payment = PaymentEntity.from(
            PaymentCommand.Create.of(
                1L, "1351039135", "SAMSUNG", "1234-5678-9814-1451", 
                "5000", "http://localhost:8080/api/v1/examples/callback"
            )
        );

        when(pgV1FeignClient.request(eq("135135"), any(PgV1Dto.Request.Transaction.class)))
            .thenThrow(new RuntimeException("PG 시스템 오류"));

        // when & then
        assertThatThrownBy(() -> pgPaymentGateway.requestPayment(payment))
            .isInstanceOf(CoreException.class)
            .hasMessageContaining("PG 결제 요청 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("PG 트랜잭션 상세 조회 성공")
    void getPaymentTransactionDetail_Success() {
        // given
        String transactionKey = "20250816:TR:9577c5";
        
        PgV1Dto.Response.Transaction transactionData = PgV1Dto.Response.Transaction.builder()
            .transactionKey("20250816:TR:9577c5")
            .orderId("1351039135")
            .cardType("SAMSUNG")
            .cardNo("1234-5678-9814-1451")
            .amount(5000L)
            .status("approved")
            .reason(null)
            .build();

        ApiResponse<PgV1Dto.Response.Transaction> expectedResponse = ApiResponse.success(transactionData);

        when(pgV1FeignClient.findTransaction(eq(transactionKey), eq("135135")))
            .thenReturn(expectedResponse);

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
