package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentFacade paymentFacade;

    private PaymentCriteria criteria;
    private PaymentInfo paymentInfo;

    @BeforeEach
    void setUp() {
        criteria = new PaymentCriteria(
            "135135",
            "1351039135",
            "SAMSUNG",
            "1234-5678-9814-1451",
            "5000",
            "http://localhost:8080/api/v1/examples/callback"
        );

        paymentInfo = PaymentInfo.of(
            "20250816:TR:12345",
            "1351039135",
            "SAMSUNG",
            "1234-5678-9814-1451",
            5000L,
            "PENDING",
            null
        );
    }

    @DisplayName("결제 처리")
    @Nested
    class ProcessPayment {
        
        @DisplayName("결제 요청이 성공하면, 성공 결과를 반환한다.")
        @Test
        void returnsSuccessResult_whenPaymentSucceeds() {
            // arrange
            when(paymentGateway.requestPayment(any()))
                .thenReturn(paymentInfo);

            // act
            PaymentResult result = paymentFacade.pay(criteria);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo("1351039135");
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.paymentId()).isEqualTo("20250816:TR:12345");
            
            verify(paymentGateway).requestPayment(any());
        }
    }

    @DisplayName("트랜잭션 상세 조회")
    @Nested
    class GetPaymentTransactionDetail {
        
        @DisplayName("트랜잭션 키로 결제 정보를 조회한다.")
        @Test
        void findsPaymentByTransactionKey() {
            // arrange
            String transactionKey = "20250816:TR:12345";
            when(paymentGateway.getPaymentTransactionDetail(transactionKey))
                .thenReturn(paymentInfo);

            // act
            PaymentInfo result = paymentFacade.getPaymentTransactionDetail(transactionKey);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.transactionKey()).isEqualTo(transactionKey);
            verify(paymentGateway).getPaymentTransactionDetail(transactionKey);
        }
    }

    @DisplayName("주문별 결제 정보 조회")
    @Nested
    class GetPaymentByOrderId {
        
        @DisplayName("주문 ID로 결제 정보를 조회한다.")
        @Test
        void findsPaymentByOrderId() {
            // arrange
            String userId = "135135";
            String orderId = "1351039135";
            // TODO: PaymentService 구현 후 테스트 수정

            // act & assert
            assertThatThrownBy(() -> paymentFacade.getPaymentByOrderId(userId, orderId))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
