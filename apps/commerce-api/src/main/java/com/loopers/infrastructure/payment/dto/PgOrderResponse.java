package com.loopers.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record PgOrderResponse(
    @JsonProperty("orderId")
    String orderId,
    
    @JsonProperty("transactions")
    List<PgTransactionResponse> transactions
) {
}
