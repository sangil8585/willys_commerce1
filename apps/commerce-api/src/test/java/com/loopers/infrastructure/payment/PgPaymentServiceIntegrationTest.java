package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PgPaymentServiceIntegrationTest {

    @Autowired
    private PgPaymentService pgPaymentService;

    @Test
    @DisplayName("실제 PG 시뮬레이터와 통신하여 결제 요청 성공")
    void 실제_결제요청_성공() {
        // given
        PgPaymentRequest request = PgPaymentRequest.of(
            "1351039135", "SAMSUNG", "1234-5678-9814-1451", "5000", 
            "http://localhost:8080/api/v1/examples/callback"
        );

        // when - 실제 PG 시뮬레이터로 요청 전송
        PgPaymentResponse result = pgPaymentService.requestPayment(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.transactionKey()).isNotEmpty();
        assertThat(result.status()).isIn("PENDING", "SUCCESS");
        
        System.out.println("결제 요청 성공!");
        System.out.println("트랜잭션 키: " + result.transactionKey());
        System.out.println("상태: " + result.status());
    }

    @Test
    @DisplayName("실제 PG 시뮬레이터에서 트랜잭션 상세 조회")
    void 실제_트랜잭션_상세조회() {
        // given - 먼저 결제 요청
        PgPaymentRequest request = PgPaymentRequest.of(
            "1351039136", "KB", "9876-5432-1098-7654", "10000", 
            "http://localhost:8080/api/v1/examples/callback"
        );
        
        PgPaymentResponse paymentResponse = pgPaymentService.requestPayment(request);
        String transactionKey = paymentResponse.transactionKey();

        // when - 트랜잭션 상세 조회
        PgTransactionDetailResponse detail = pgPaymentService.getTransactionDetail(transactionKey);

        // then
        assertThat(detail).isNotNull();
        assertThat(detail.transactionKey()).isEqualTo(transactionKey);
        assertThat(detail.orderId()).isEqualTo("1351039136");
        
        System.out.println("트랜잭션 상세 조회 성공!");
        System.out.println("주문 ID: " + detail.orderId());
        System.out.println("카드 타입: " + detail.cardType());
        System.out.println("금액: " + detail.amount());
        System.out.println("상태: " + detail.status());
    }
}
