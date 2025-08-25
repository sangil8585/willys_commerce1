package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentV1ApiE2ETest {

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    
    @MockBean
    private PaymentFacade paymentFacade;

    @Autowired
    public PaymentV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
    }

    @BeforeEach
    void setUp() {
        // Mock 설정 - 결제 성공 시나리오
        setupMockPaymentSuccess();
    }
    
    private void setupMockPaymentSuccess() {
        PaymentResult successResult = new PaymentResult(
            "1351039135",
            "20250816:TR:9577c5",
            "PENDING"
        );
        when(paymentFacade.pay(any())).thenReturn(successResult);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }
    
    private void setupMockPaymentRejection() {
        when(paymentFacade.pay(any()))
            .thenThrow(new RuntimeException("카드 한도 초과로 결제가 거절되었습니다."));
    }
    
    private void setupMockPaymentTimeout() {
        when(paymentFacade.pay(any()))
            .thenThrow(new RuntimeException("결제 처리 시간이 초과되었습니다."));
    }

    @DisplayName("POST /api/v1/payments")
    @Nested
    class RequestPayment {
        
        @DisplayName("유효한 카드 결제 요청을 보내면, 결제 응답을 반환한다.")
        @Test
        void returnsPaymentResponse_whenValidCardPaymentRequestIsProvided() {
            // arrange
            String userId = "135135";
            PaymentV1Dto.PaymentRequest request = new PaymentV1Dto.PaymentRequest(
                "1351039135",
                "SAMSUNG",
                "1234-5678-9814-1451",
                "5000",
                "http://localhost:8080/api/v1/examples/callback"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<PaymentV1Dto.PaymentRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentResponse>> response =
                testRestTemplate.exchange("/api/v1/payments", HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo("1351039135"),
                () -> assertThat(response.getBody().data().status()).isEqualTo("PENDING"),
                () -> assertThat(response.getBody().data().paymentId()).isNotNull(),
                () -> assertThat(response.getBody().data().message()).isEqualTo("결제 요청이 성공적으로 접수되었습니다.")
            );
        }

        @DisplayName("사용자 ID 헤더가 없으면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void throwsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            PaymentV1Dto.PaymentRequest request = new PaymentV1Dto.PaymentRequest(
                "1351039135",
                "SAMSUNG",
                "1234-5678-9814-1451",
                "5000",
                "http://localhost:8080/api/v1/examples/callback"
            );
            
            HttpEntity<PaymentV1Dto.PaymentRequest> httpEntity = new HttpEntity<>(request);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentResponse>> response =
                testRestTemplate.exchange("/api/v1/payments", HttpMethod.POST, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
        
        @DisplayName("필수 필드가 누락되면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void throwsBadRequest_whenRequiredFieldsAreMissing() {
            // arrange
            String userId = "135135";
            PaymentV1Dto.PaymentRequest request = new PaymentV1Dto.PaymentRequest(
                null, // orderId 누락
                "SAMSUNG",
                "1234-5678-9814-1451",
                "5000",
                "http://localhost:8080/api/v1/examples/callback"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<PaymentV1Dto.PaymentRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentResponse>> response =
                testRestTemplate.exchange("/api/v1/payments", HttpMethod.POST, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
        
        @DisplayName("결제가 거절되면, 적절한 에러 응답을 받는다.")
        @Test
        void throwsError_whenPaymentIsRejected() {
            // arrange - Mock 설정을 결제 거절로 변경
            setupMockPaymentRejection();
            
            String userId = "135135";
            PaymentV1Dto.PaymentRequest request = new PaymentV1Dto.PaymentRequest(
                "1351039135",
                "SAMSUNG",
                "1234-5678-9814-1451",
                "5000",
                "http://localhost:8080/api/v1/examples/callback"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<PaymentV1Dto.PaymentRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentResponse>> response =
                testRestTemplate.exchange("/api/v1/payments", HttpMethod.POST, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is5xxServerError());
        }
        
        @DisplayName("결제 처리 시간이 초과되면, 적절한 에러 응답을 받는다.")
        @Test
        void throwsError_whenPaymentTimeout() {
            // arrange - Mock 설정을 결제 타임아웃으로 변경
            setupMockPaymentTimeout();
            
            String userId = "135135";
            PaymentV1Dto.PaymentRequest request = new PaymentV1Dto.PaymentRequest(
                "1351039135",
                "SAMSUNG",
                "1234-5678-9814-1451",
                "5000",
                "http://localhost:8080/api/v1/examples/callback"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<PaymentV1Dto.PaymentRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentResponse>> response =
                testRestTemplate.exchange("/api/v1/payments", HttpMethod.POST, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is5xxServerError());
        }
    }

    @DisplayName("GET /api/v1/payments/{paymentId}")
    @Nested
    class GetPaymentInfo {
        
        @DisplayName("유효한 결제 ID로 요청하면, 결제 정보 응답을 반환한다.")
        @Test
        void returnsPaymentInfoResponse_whenValidPaymentIdIsProvided() {
            // arrange
            String userId = "135135";
            String paymentId = "20250816:TR:9577c5";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> response =
                testRestTemplate.exchange("/api/v1/payments/" + paymentId, 
                    HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().paymentId()).isEqualTo(paymentId),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo("ORDER-001"),
                () -> assertThat(response.getBody().data().cardType()).isEqualTo("SAMSUNG"),
                () -> assertThat(response.getBody().data().cardNo()).isEqualTo("1234-5678-9814-1451"),
                () -> assertThat(response.getBody().data().amount()).isEqualTo("5000"),
                () -> assertThat(response.getBody().data().status()).isEqualTo("PENDING"),
                () -> assertThat(response.getBody().data().callbackUrl()).isEqualTo("http://localhost:8080/api/v1/examples/callback"),
                () -> assertThat(response.getBody().data().createdAt()).isNotNull()
            );
        }

        @DisplayName("사용자 ID 헤더가 없으면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void throwsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            String paymentId = "20250816:TR:9577c5";
            
            HttpEntity<Void> httpEntity = new HttpEntity<>(null);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> response =
                testRestTemplate.exchange("/api/v1/payments/" + paymentId, 
                    HttpMethod.GET, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
    }

    @DisplayName("GET /api/v1/payments?orderId={orderId}")
    @Nested
    class GetPaymentByOrderId {
        
        @DisplayName("유효한 주문 ID로 요청하면, 주문별 결제 정보 응답을 반환한다.")
        @Test
        void returnsPaymentInfoResponse_whenValidOrderIdIsProvided() {
            // arrange
            String userId = "135135";
            String orderId = "1351039135";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> response =
                testRestTemplate.exchange("/api/v1/payments?orderId=" + orderId, 
                    HttpMethod.GET, httpEntity, responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().paymentId()).isEqualTo("20250816:TR:9577c5"),
                () -> assertThat(response.getBody().data().orderId()).isEqualTo(orderId),
                () -> assertThat(response.getBody().data().cardType()).isEqualTo("SAMSUNG"),
                () -> assertThat(response.getBody().data().cardNo()).isEqualTo("1234-5678-9814-1451"),
                () -> assertThat(response.getBody().data().amount()).isEqualTo("5000"),
                () -> assertThat(response.getBody().data().status()).isEqualTo("PENDING"),
                () -> assertThat(response.getBody().data().callbackUrl()).isEqualTo("http://localhost:8080/api/v1/examples/callback"),
                () -> assertThat(response.getBody().data().createdAt()).isNotNull()
            );
        }

        @DisplayName("사용자 ID 헤더가 없으면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void throwsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            String orderId = "1351039135";
            
            HttpEntity<Void> httpEntity = new HttpEntity<>(null);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> response =
                testRestTemplate.exchange("/api/v1/payments?orderId=" + orderId, 
                    HttpMethod.GET, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
        
        @DisplayName("주문 ID 파라미터가 없으면, 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void throwsBadRequest_whenOrderIdParameterIsMissing() {
            // arrange
            String userId = "135135";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> responseType = 
                new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PaymentV1Dto.PaymentInfoResponse>> response =
                testRestTemplate.exchange("/api/v1/payments", 
                    HttpMethod.GET, httpEntity, responseType);

            // assert
            assertTrue(response.getStatusCode().is4xxClientError());
        }
    }
}
