package com.loopers.config.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@EnableFeignClients(basePackages = "com.loopers.infrastructure.payment")
public class PgPaymentConfig {
    
    // FeignClient가 자동으로 활성화됩니다.
    // 별도의 RestTemplate 설정이 필요하지 않습니다.
}
