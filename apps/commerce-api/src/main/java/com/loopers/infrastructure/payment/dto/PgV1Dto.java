package com.loopers.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public class PgV1Dto {

    public static class Request {
        
        @Builder
        public record Transaction(
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
            public static Transaction of(String orderId, String cardType, String cardNo, String amount, String callbackUrl) {
                return new Transaction(orderId, cardType, cardNo, amount, callbackUrl);
            }
        }
    }

    public static class Response {
        
        @Builder
        public record Transaction(
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
        
        @Builder
        public record Order(
            @JsonProperty("orderId")
            String orderId,
            
            @JsonProperty("status")
            String status,
            
            @JsonProperty("amount")
            Long amount
        ) {
        }
    }
}
