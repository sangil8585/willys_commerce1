package com.loopers.application.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PgTransactionDetailResponse(
    @JsonProperty("transactionKey")
    String transactionKey,
    
    @JsonProperty("orderId")
    String orderId,
    
    @JsonProperty("cardType")
    String cardType,
    
    @JsonProperty("cardNo")
    String cardNo,
    
    @JsonProperty("amount")
    Long amount,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("reason")
    String reason
) {
}
