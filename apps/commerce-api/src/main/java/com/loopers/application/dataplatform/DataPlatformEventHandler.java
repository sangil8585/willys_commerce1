package com.loopers.application.dataplatform;

import com.loopers.domain.dataplatform.DataPlatformGateway;
import com.loopers.domain.order.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformEventHandler {

    private final DataPlatformGateway dataPlatformGateway;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderDataPlatformSent(OrderEvent.DataPlatformSent event) {
        dataPlatformGateway.sendOrderData(event.orderId(), event.userId(), event.eventType());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentCompleted(OrderEvent.PaymentCompleted event) {
        dataPlatformGateway.sendPaymentData(event.orderId(), event.paymentId(), event.finalAmount());
    }
}
