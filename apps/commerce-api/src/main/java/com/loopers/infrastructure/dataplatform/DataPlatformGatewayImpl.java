package com.loopers.infrastructure.dataplatform;

import com.loopers.domain.dataplatform.DataPlatformGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataPlatformGatewayImpl implements DataPlatformGateway {

    @Override
    public void sendOrderData(Long orderId, Long userId, String eventType) {
        
    }

    @Override
    public void sendPaymentData(Long orderId, String paymentId, Long finalAmount) {
        
    }
}
