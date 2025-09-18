package com.loopers.batch.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * HelloWorld 배치 통합 테스트
 * IntelliJ에서 쉽게 실행 가능
 */
@SpringBootTest
class HelloWorldBatchTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job helloWorldJob;

    @Test
    void testHelloWorldBatch() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        // When & Then
        jobLauncher.run(helloWorldJob, jobParameters);
    }
}
