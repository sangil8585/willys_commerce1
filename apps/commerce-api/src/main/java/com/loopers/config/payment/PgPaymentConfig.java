package com.loopers.config.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Configuration
public class PgPaymentConfig {
    
    @Value("${pg-simulator.connection-timeout:5000}")
    private int connectionTimeout;
    
    @Value("${pg-simulator.read-timeout:10000}")
    private int readTimeout;
    
    @Bean("pgPaymentRestTemplate")
    public RestTemplate pgPaymentRestTemplate() {
        log.info("PG 결제용 RestTemplate 설정: connectionTimeout={}ms, readTimeout={}ms", 
            connectionTimeout, readTimeout);
        
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
