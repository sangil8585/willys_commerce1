package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private PaymentCommand.Create createCommand;
    private PaymentEntity paymentEntity;

    @BeforeEach
    void setUp() {
        createCommand = PaymentCommand.Create.of(
            135135L,
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

    @DisplayName("결제 생성 통합 테스트")
    @Nested
    class CreatePaymentIntegration {
        
        @DisplayName("유효한 결제 요청이면, DB에 결제를 저장한다.")
        @Test
        void createsPaymentInDatabase_whenValidRequest() {
            // act
            PaymentEntity result = paymentService.createPayment(createCommand);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo("1351039135");
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            
            // DB에서 실제로 저장되었는지 확인
            Optional<PaymentEntity> savedPayment = paymentRepository.findByOrderId("1351039135");
            assertThat(savedPayment).isPresent();
            assertThat(savedPayment.get().getOrderId()).isEqualTo("1351039135");
        }

        @DisplayName("이미 결제가 진행 중인 주문이면, 예외를 발생시킨다.")
        @Test
        void throwsException_whenPaymentAlreadyExists() {
            // arrange - 첫 번째 결제 생성
            paymentService.createPayment(createCommand);

            // act & assert - 동일한 주문으로 두 번째 결제 시도
            assertThatThrownBy(() -> paymentService.createPayment(createCommand))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST)
                .hasMessage("이미 결제가 진행 중인 주문입니다: 1351039135");
        }
    }

    @DisplayName("결제 상태 업데이트 통합 테스트")
    @Nested
    class UpdatePaymentStatusIntegration {
        
        @BeforeEach
        void setUpPayment() {
            paymentEntity = paymentService.createPayment(createCommand);
        }
        
        @DisplayName("결제 ID 할당을 성공한다.")
        @Test
        void assignsPaymentId_successfully() {
            // arrange
            PaymentCommand.AssignPaymentId assignCommand = 
                PaymentCommand.AssignPaymentId.of("PG_TRANSACTION_ID");

            // act
            paymentService.assignPaymentId(paymentEntity, assignCommand);

            // assert
            PaymentEntity updatedPayment = paymentRepository.findByOrderId("1351039135").orElseThrow();
            assertThat(updatedPayment.getPaymentId()).isEqualTo("PG_TRANSACTION_ID");
        }

        @DisplayName("결제 상태를 업데이트한다.")
        @Test
        void updatesPaymentStatus_successfully() {
            // arrange
            PaymentCommand.UpdateStatus updateCommand = 
                PaymentCommand.UpdateStatus.of(PaymentStatus.PROCESSING);

            // act
            paymentService.updatePaymentStatus(paymentEntity, updateCommand);

            // assert
            PaymentEntity updatedPayment = paymentRepository.findByOrderId("1351039135").orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }
    }

    @DisplayName("결제 완료 통합 테스트")
    @Nested
    class CompletePaymentIntegration {
        
        @BeforeEach
        void setUpPayment() {
            paymentEntity = paymentService.createPayment(createCommand);
        }
        
        @DisplayName("결제를 완료한다.")
        @Test
        void completesPayment_successfully() {
            // arrange
            PaymentCommand.Complete completeCommand = 
                PaymentCommand.Complete.of("SUCCESS");

            // act
            paymentService.completePayment(paymentEntity, completeCommand);

            // assert
            PaymentEntity updatedPayment = paymentRepository.findByOrderId("1351039135").orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(updatedPayment.getTransactionId()).isEqualTo("SUCCESS");
        }
    }

    @DisplayName("결제 실패 통합 테스트")
    @Nested
    class FailPaymentIntegration {
        
        @BeforeEach
        void setUpPayment() {
            paymentEntity = paymentService.createPayment(createCommand);
        }
        
        @DisplayName("결제를 실패 처리한다.")
        @Test
        void failsPayment_successfully() {
            // arrange
            PaymentCommand.Fail failCommand = 
                PaymentCommand.Fail.of("카드 한도 초과");

            // act
            paymentService.failPayment(paymentEntity, failCommand);

            // assert
            PaymentEntity updatedPayment = paymentRepository.findByOrderId("1351039135").orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(updatedPayment.getErrorMessage()).isEqualTo("카드 한도 초과");
        }
    }

    @DisplayName("결제 조회 통합 테스트")
    @Nested
    class FindPaymentIntegration {
        
        @BeforeEach
        void setUpPayment() {
            paymentEntity = paymentService.createPayment(createCommand);
        }
        
        @DisplayName("결제 ID로 결제를 조회한다.")
        @Test
        void findsPaymentByPaymentId() {
            // arrange
            String paymentId = paymentEntity.getPaymentId();

            // act
            PaymentEntity result = paymentService.findByPaymentId(paymentId);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo("1351039135");
        }

        @DisplayName("주문 ID로 결제를 조회한다.")
        @Test
        void findsPaymentByOrderId() {
            // arrange
            String orderId = "1351039135";

            // act
            PaymentEntity result = paymentService.findByOrderId(orderId);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
        }

        @DisplayName("사용자 ID로 결제 목록을 조회한다.")
        @Test
        void findsPaymentsByUserId() {
            // arrange
            Long userId = 135135L;

            // act
            List<PaymentEntity> result = paymentService.findByUserId(userId);

            // assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(userId);
        }

        @DisplayName("대기 중인 결제 목록을 조회한다.")
        @Test
        void findsPendingPayments() {
            // act
            List<PaymentEntity> result = paymentService.findPendingPayments();

            // assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
        }
    }

    @DisplayName("결제 삭제 통합 테스트")
    @Nested
    class DeletePaymentIntegration {
        
        @BeforeEach
        void setUpPayment() {
            paymentEntity = paymentService.createPayment(createCommand);
        }
        
        @DisplayName("결제를 삭제한다.")
        @Test
        void deletesPayment_successfully() {
            // act
            paymentService.deletePayment(paymentEntity);

            // assert
            Optional<PaymentEntity> deletedPayment = paymentRepository.findByOrderId("1351039135");
            assertThat(deletedPayment).isEmpty();
        }
    }
}
