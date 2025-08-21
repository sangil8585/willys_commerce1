package com.loopers.application.payment.pg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PgPaymentRequest(
    @JsonProperty("orderId")
    String orderId,
    
    @JsonProperty("cardType")
    String cardType,
    
    @JsonProperty("cardNo")
    String cardNo,
    
    @JsonProperty("amount")
    String amount,
    
    @JsonProperty("callbackUrl")
    String callbackUrl
) {
    public static PgPaymentRequest of(String orderId, String cardType, String cardNo, String amount, String callbackUrl) {
        return PgPaymentRequest.builder()
            .orderId(orderId)
            .cardType(cardType)
            .cardNo(cardNo)
            .amount(amount)
            .callbackUrl(callbackUrl)
            .build();
    }
}
