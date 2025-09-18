package com.loopers;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableAsync                    // 비동기 메서드 실행 활성화 (배치 작업 처리 시 오래걸릴 때 성능 최적화)
@EnableScheduling               // 스케줄링 기능 활성화 (주/월간 집계 자동 실행)
@ConfigurationPropertiesScan    // 이 어노테이션이 붙은 클래스를 빈 등록 (배치 설정을 유연하게 관리한다)
@SpringBootApplication
public class CommerceBatchApplication {

    @PostConstruct
    public void started() {
        // set timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
    public static void main(String[] args) {
        SpringApplication.run(CommerceBatchApplication.class, args);
    }
}
