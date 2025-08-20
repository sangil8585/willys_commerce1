package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.PaymentV1Dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Payment V1 API")
public interface PaymentV1ApiSpec {
    
    @Operation(
            summary = "결제 요청",
            description = "PG 기반 카드 결제 요청을 처리하고 결제 ID를 생성합니다."
    )
    ApiResponse<PaymentResponse> requestPayment(
            @Schema(name = "사용자 ID", description = "결제 요청하는 사용자의 ID")
            @RequestHeader("X-USER-ID") String userId,
            @Schema(name = "결제 요청", description = "카드 결제 요청 객체")
            @RequestBody PaymentRequest request
    );
    
    @Operation(
            summary = "결제 정보 확인",
            description = "결제 ID로 결제 정보를 조회합니다."
    )
    PaymentInfoResponse getPaymentInfo(
            @Schema(name = "사용자 ID", description = "결제 정보를 조회하는 사용자의 ID")
            @RequestHeader("X-USER-ID") String userId,
            @Schema(name = "결제 ID", description = "조회할 결제의 ID")
            String paymentId
    );
    
    @Operation(
            summary = "주문별 결제 정보 조회",
            description = "주문 ID로 연관된 결제 정보를 조회합니다."
    )
    PaymentInfoResponse getPaymentByOrderId(
            @Schema(name = "사용자 ID", description = "결제 정보를 조회하는 사용자의 ID")
            @RequestHeader("X-USER-ID") String userId,
            @Schema(name = "주문 ID", description = "조회할 주문의 ID")
            @RequestParam String orderId
    );
}
