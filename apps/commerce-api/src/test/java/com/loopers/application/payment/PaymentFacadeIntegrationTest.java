package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentFacadeIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;



    private PaymentCriteria criteria;
    private PaymentEntity paymentEntity;

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
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("결제 처리 통합 테스트")
    @Nested
    class ProcessPaymentIntegration {
        
        @DisplayName("결제 요청이 성공하면, 성공 결과를 반환한다.")
        @Test
        void returnsSuccessResult_whenPaymentSucceeds() {
            // act
            PaymentResult result = paymentFacade.pay(criteria);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo("1351039135");
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.paymentId()).isNotNull();
        }
    }

    @DisplayName("주문별 결제 정보 조회 통합 테스트")
    @Nested
    class GetPaymentByOrderIdIntegration {
        
        @BeforeEach
        void setUpPayment() {
            // 실제 결제 데이터 생성
            paymentEntity = paymentService.createPayment(
                com.loopers.domain.payment.PaymentCommand.Create.of(
                    135135L,
                    "1351039135",
                    "SAMSUNG",
                    "1234-5678-9814-1451",
                    "5000",
                    "http://localhost:8080/api/v1/examples/callback"
                )
            );
        }
        
        @DisplayName("주문 ID로 결제 정보를 조회한다.")
        @Test
        void findsPaymentByOrderId() {
            // arrange
            String userId = "135135";
            String orderId = "1351039135";

            // act
            PaymentResult result = paymentFacade.getPaymentByOrderId(userId, orderId);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
        }

        @DisplayName("존재하지 않는 주문 ID로 조회하면, 예외를 발생시킨다.")
        @Test
        void throwsException_whenOrderIdNotFound() {
            // arrange
            String userId = "135135";
            String orderId = "NON_EXISTENT";

            // act & assert
            assertThatThrownBy(() -> paymentFacade.getPaymentByOrderId(userId, orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("주문을 찾을 수 없습니다");
        }
    }


}
