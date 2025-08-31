package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.OrderCommand;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1Spec {
    
    private final OrderFacade orderFacade;
    
    /**
     * 주문을 생성합니다.
     */
    @Override
    @PostMapping
    public ApiResponse<OrderV1Dto.Response.Order> createOrder(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody OrderV1Dto.Request.Order request
    ) {
        OrderCriteria.Order criteria = request.toCriteria();
        var result = orderFacade.createOrder(criteria);

        return ApiResponse.success(OrderV1Dto.Response.Order.from(result));
        
    }
}
