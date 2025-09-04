package com.loopers.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @GetMapping
    public Map<String, Object> health() {
        log.info("Health check 요청 - commerce-collector");
        return Map.of(
            "status", "UP",
            "service", "commerce-collector",
            "timestamp", LocalDateTime.now()
        );
    }
}
