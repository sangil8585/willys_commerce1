package com.loopers.config.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
@ConditionalOnProperty(name = "spring.redis.host")
public class SimpleRedisConfig {
    // 기본 Redis 설정은 application.yml의 spring.redis.* 속성을 사용
}
