package com.loopers.application.payment;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;
    private final OrderFacade orderFacade;
    
    @Transactional
    public PaymentResult pay(PaymentCriteria criteria) {
        try {
            // 결제 명령 생성
            PaymentCommand.Create paymentCommand = PaymentCommand.Create.of(
                Long.valueOf(criteria.userId()),
                criteria.orderId(),
                criteria.cardType(),
                criteria.cardNo(),
                criteria.amount(),
                criteria.callbackUrl()
            );
            
            // 결제 정보 저장
            PaymentEntity payment = paymentService.createPayment(paymentCommand);
            
            // PG 결제 요청
            PaymentInfo paymentInfo = paymentGateway.requestPayment(payment);
            
            // 결제 ID 할당
            paymentService.assignPaymentId(payment, PaymentCommand.AssignPaymentId.of(paymentInfo.transactionKey()));
            
            return new PaymentResult(
                criteria.orderId(),
                paymentInfo.transactionKey(),
                paymentInfo.status()
            );
            
        } catch (Exception e) {
            log.error("결제 요청 실패 - orderId: {}, error: {}", criteria.orderId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 결제 완료 콜백 처리
     * PG에서 결제 완료 시 호출되는 메서드
     */
    @Transactional
    public void handlePaymentCallback(String paymentId, String transactionId, boolean isSuccess, String errorMessage) {
        PaymentEntity payment = paymentService.findByPaymentId(paymentId);
        Long orderId = Long.valueOf(payment.getOrderId());
        
        try {
            if (isSuccess) {
                // 결제 완료 처리
                paymentService.completePayment(payment, PaymentCommand.Complete.of(transactionId));
                log.info("결제 완료 - paymentId: {}, orderId: {}, transactionId: {}", paymentId, orderId, transactionId);
                
                // 주문 도메인에 결제 완료 알림 - 주문 완료 처리 및 재고 차감
                orderFacade.completeOrderAfterPayment(orderId);
                
            } else {
                // 결제 실패 처리
                paymentService.failPayment(payment, PaymentCommand.Fail.of(errorMessage));
                log.error("결제 실패 - paymentId: {}, orderId: {}, error: {}", paymentId, orderId, errorMessage);
                
                // 주문 도메인에 결제 실패 알림 - 주문 취소 처리
                orderFacade.cancelOrderAfterPaymentFailure(orderId, errorMessage);
            }
            
        } catch (Exception e) {
            log.error("결제 콜백 처리 중 오류 발생 - paymentId: {}, orderId: {}, error: {}", 
                    paymentId, orderId, e.getMessage());
            
            // 결제 실패로 처리
            paymentService.failPayment(payment, PaymentCommand.Fail.of("콜백 처리 중 오류: " + e.getMessage()));
            orderFacade.cancelOrderAfterPaymentFailure(orderId, "결제 처리 중 오류 발생");
            
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public PaymentInfo getPaymentTransactionDetail(String transactionKey) {
        return paymentGateway.getPaymentTransactionDetail(transactionKey);
    }
    
    @Transactional(readOnly = true)
    public PaymentResult getPaymentByOrderId(Long userId, String orderId) {
        // 실제 구현에서는 PaymentService를 통해 결제 정보를 조회
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}
