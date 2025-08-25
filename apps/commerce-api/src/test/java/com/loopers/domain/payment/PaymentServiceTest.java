package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

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

        paymentEntity = PaymentEntity.from(createCommand);
    }

    @DisplayName("결제 생성")
    @Nested
    class CreatePayment {
        
        @DisplayName("유효한 결제 요청이면, 결제를 생성한다.")
        @Test
        void createsPayment_whenValidRequest() {
            // arrange
            when(paymentRepository.findByOrderId(createCommand.orderId()))
                .thenReturn(Optional.empty());
            when(paymentRepository.save(any(PaymentEntity.class)))
                .thenReturn(paymentEntity);

            // act
            PaymentEntity result = paymentService.createPayment(createCommand);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo("1351039135");
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            
            verify(paymentRepository).findByOrderId(createCommand.orderId());
            verify(paymentRepository).save(any(PaymentEntity.class));
        }

        @DisplayName("이미 결제가 진행 중인 주문이면, 예외를 발생시킨다.")
        @Test
        void throwsException_whenPaymentAlreadyExists() {
            // arrange
            when(paymentRepository.findByOrderId(createCommand.orderId()))
                .thenReturn(Optional.of(paymentEntity));

            // act & assert
            assertThatThrownBy(() -> paymentService.createPayment(createCommand))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST)
                .hasMessage("이미 결제가 진행 중인 주문입니다: 1351039135");

            verify(paymentRepository).findByOrderId(createCommand.orderId());
            verify(paymentRepository, never()).save(any());
        }
    }

    @DisplayName("결제 상태 업데이트")
    @Nested
    class UpdatePaymentStatus {
        
        @DisplayName("결제 ID 할당을 성공한다.")
        @Test
        void assignsPaymentId_successfully() {
            // arrange
            PaymentCommand.AssignPaymentId assignCommand = 
                new PaymentCommand.AssignPaymentId("PG_TRANSACTION_ID");
            when(paymentRepository.save(any(PaymentEntity.class)))
                .thenReturn(paymentEntity);

            // act
            paymentService.assignPaymentId(paymentEntity, assignCommand);

            // assert
            verify(paymentRepository).save(paymentEntity);
        }

        @DisplayName("결제 상태를 업데이트한다.")
        @Test
        void updatesPaymentStatus_successfully() {
            // arrange
            PaymentCommand.UpdateStatus updateCommand = 
                new PaymentCommand.UpdateStatus(PaymentStatus.PROCESSING);
            when(paymentRepository.save(any(PaymentEntity.class)))
                .thenReturn(paymentEntity);

            // act
            paymentService.updatePaymentStatus(paymentEntity, updateCommand);

            // assert
            verify(paymentRepository).save(paymentEntity);
        }
    }

    @DisplayName("결제 완료")
    @Nested
    class CompletePayment {
        
        @DisplayName("결제를 완료한다.")
        @Test
        void completesPayment_successfully() {
            // arrange
            PaymentCommand.Complete completeCommand = 
                PaymentCommand.Complete.of("SUCCESS");
            when(paymentRepository.save(any(PaymentEntity.class)))
                .thenReturn(paymentEntity);

            // act
            paymentService.completePayment(paymentEntity, completeCommand);

            // assert
            verify(paymentRepository).save(paymentEntity);
        }
    }

    @DisplayName("결제 실패")
    @Nested
    class FailPayment {
        
        @DisplayName("결제를 실패 처리한다.")
        @Test
        void failsPayment_successfully() {
            // arrange
            PaymentCommand.Fail failCommand = 
                PaymentCommand.Fail.of("카드 한도 초과");
            when(paymentRepository.save(any(PaymentEntity.class)))
                .thenReturn(paymentEntity);

            // act
            paymentService.failPayment(paymentEntity, failCommand);

            // assert
            verify(paymentRepository).save(paymentEntity);
        }
    }

    @DisplayName("결제 조회")
    @Nested
    class FindPayment {
        
        @DisplayName("결제 ID로 결제를 조회한다.")
        @Test
        void findsPaymentByPaymentId() {
            // arrange
            String paymentId = "20250816:TR:9577c5";
            when(paymentRepository.findByPaymentId(paymentId))
                .thenReturn(Optional.of(paymentEntity));

            // act
            PaymentEntity result = paymentService.findByPaymentId(paymentId);

            // assert
            assertThat(result).isEqualTo(paymentEntity);
            verify(paymentRepository).findByPaymentId(paymentId);
        }

        @DisplayName("존재하지 않는 결제 ID로 조회하면, 예외를 발생시킨다.")
        @Test
        void throwsException_whenPaymentIdNotFound() {
            // arrange
            String paymentId = "NON_EXISTENT";
            when(paymentRepository.findByPaymentId(paymentId))
                .thenReturn(Optional.empty());

            // act & assert
            assertThatThrownBy(() -> paymentService.findByPaymentId(paymentId))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
                .hasMessage("결제 정보를 찾을 수 없습니다: NON_EXISTENT");

            verify(paymentRepository).findByPaymentId(paymentId);
        }

        @DisplayName("주문 ID로 결제를 조회한다.")
        @Test
        void findsPaymentByOrderId() {
            // arrange
            String orderId = "1351039135";
            when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(paymentEntity));

            // act
            PaymentEntity result = paymentService.findByOrderId(orderId);

            // assert
            assertThat(result).isEqualTo(paymentEntity);
            verify(paymentRepository).findByOrderId(orderId);
        }

        @DisplayName("사용자 ID로 결제 목록을 조회한다.")
        @Test
        void findsPaymentsByUserId() {
            // arrange
            Long userId = 135135L;
            List<PaymentEntity> payments = List.of(paymentEntity);
            when(paymentRepository.findByUserId(userId))
                .thenReturn(payments);

            // act
            List<PaymentEntity> result = paymentService.findByUserId(userId);

            // assert
            assertThat(result).hasSize(1);
            assertThat(result).contains(paymentEntity);
            verify(paymentRepository).findByUserId(userId);
        }

        @DisplayName("대기 중인 결제 목록을 조회한다.")
        @Test
        void findsPendingPayments() {
            // arrange
            List<PaymentEntity> pendingPayments = List.of(paymentEntity);
            when(paymentRepository.findPendingPayments())
                .thenReturn(pendingPayments);

            // act
            List<PaymentEntity> result = paymentService.findPendingPayments();

            // assert
            assertThat(result).hasSize(1);
            assertThat(result).contains(paymentEntity);
            verify(paymentRepository).findPendingPayments();
        }
    }

    @DisplayName("결제 삭제")
    @Nested
    class DeletePayment {
        
        @DisplayName("결제를 삭제한다.")
        @Test
        void deletesPayment_successfully() {
            // arrange
            doNothing().when(paymentRepository).delete(paymentEntity);

            // act
            paymentService.deletePayment(paymentEntity);

            // assert
            verify(paymentRepository).delete(paymentEntity);
        }
    }
}
