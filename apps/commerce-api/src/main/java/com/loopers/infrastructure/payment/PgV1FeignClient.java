package com.loopers.infrastructure.payment;

import com.loopers.infrastructure.payment.dto.PgV1Dto;
import com.loopers.interfaces.api.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "pgClient",
    url = "http://localhost:8082/api/v1/payments"
)
public interface PgV1FeignClient {

    @PostMapping
    @CircuitBreaker(name = "pg-payment")
    @TimeLimiter(name = "pg-payment")
    @Retry(name = "pg-payment")
    ApiResponse<PgV1Dto.Response.Transaction> request(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgV1Dto.Request.Transaction request
    );

    @GetMapping
    @CircuitBreaker(name = "pg-payment")
    @TimeLimiter(name = "pg-payment")
    @Retry(name = "pg-payment")
    ApiResponse<PgV1Dto.Response.Order> findOrder(
            @RequestParam(name = "orderId") String orderKey,
            @RequestHeader("X-USER-ID") String userId
    );

    @GetMapping("/{transactionKey}")
    @CircuitBreaker(name = "pg-payment")
    @TimeLimiter(name = "pg-payment")
    @Retry(name = "pg-payment")
    ApiResponse<PgV1Dto.Response.Transaction> findTransaction(
            @PathVariable(name = "transactionKey") String transactionKey,
            @RequestHeader("X-USER-ID") String userId
    );
}
