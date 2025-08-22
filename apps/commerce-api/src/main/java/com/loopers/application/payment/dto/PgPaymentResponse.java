package com.loopers.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PgPaymentResponse(
    @JsonProperty("transactionKey")
    String transactionKey,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("reason")
    String reason
) {
}
