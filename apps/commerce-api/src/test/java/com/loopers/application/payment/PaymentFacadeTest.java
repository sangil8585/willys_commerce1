package com.loopers.application.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @InjectMocks
    private PaymentFacade paymentFacade;

    @DisplayName("pay")
    @Test
    void pay_ReturnsPaymentResult_whenValidCriteriaProvided() {
        // arrange
        PaymentCriteria criteria = new PaymentCriteria(
            "135135",
            "1351039135",
            "SAMSUNG",
            "1234-5678-9814-1451",
            "5000",
            "http://localhost:8080/api/v1/examples/callback"
        );

        // act
        PaymentResult result = paymentFacade.pay(criteria);

        // assert
        assertThat(result.orderId()).isEqualTo("1351039135");
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.paymentId()).startsWith("20250816:TR:");
        assertThat(result.status()).isEqualTo("PENDING");
    }
}
