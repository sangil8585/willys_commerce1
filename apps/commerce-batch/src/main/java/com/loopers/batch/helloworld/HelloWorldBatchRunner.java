package com.loopers.batch.helloworld;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * HelloWorld 배치 실행기
 * IntelliJ에서 쉽게 테스트할 수 있도록 CommandLineRunner 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HelloWorldBatchRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job helloWorldJob;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Starting HelloWorld Batch Test...");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(helloWorldJob, jobParameters);
            log.info("✅ HelloWorld Batch completed successfully!");
        } catch (Exception e) {
            log.error("❌ HelloWorld Batch failed", e);
            throw e;
        }
    }
}
