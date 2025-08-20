package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;

    @PostMapping
    @Override
    public ApiResponse<PaymentV1Dto.PaymentResponse> requestPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PaymentV1Dto.PaymentRequest request
    ) {
        PaymentCriteria criteria = new PaymentCriteria(
            userId,
            request.orderId(),
            request.cardType(),
            request.cardNo(),
            request.amount(),
            request.callbackUrl()
        );

        PaymentResult result = paymentFacade.pay(criteria);

        return ApiResponse.success(PaymentV1Dto.PaymentResponse.from(result));
    }
    
    @GetMapping("/{paymentId}")
    @Override
    public PaymentV1Dto.PaymentInfoResponse getPaymentInfo(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable String paymentId
    ) {
        // TODO: 실제 구현에서는 PaymentFacade를 통해 결제 정보를 조회
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
    
    @GetMapping
    @Override
    public PaymentV1Dto.PaymentInfoResponse getPaymentByOrderId(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String orderId
    ) {
        // TODO: 실제 구현에서는 PaymentFacade를 통해 주문별 결제 정보를 조회
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}
