package com.loopers.batch.helloworld;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * HelloWorld 배치 Job 설정
 * Chunk-Oriented Processing 예제
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloWorldJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * HelloWorld Job 정의
     */
    @Bean
    public Job helloWorldJob(Step helloWorldStep) {
        log.info("Creating HelloWorld Job");
        return new JobBuilder("helloWorldJob", jobRepository)
                .start(helloWorldStep)
                .build();
    }

    /**
     * HelloWorld Step 정의 (Chunk-Oriented)
     * Reader → Processor → Writer
     */
    @Bean
    public Step helloWorldStep() {
        log.info("Creating HelloWorld Step");
        return new StepBuilder("helloWorldStep", jobRepository)
                // Chunk-Oriented: <Input, Output> 타입 지정, chunk 크기 설정
                .<HelloWorldItem, HelloWorldItem>chunk(3, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    /**
     * Reader 빈 - StepScope로 설정
     */
    @Bean
    @StepScope
    public HelloWorldReader reader() {
        return new HelloWorldReader();
    }

    /**
     * Processor 빈 - StepScope로 설정
     */
    @Bean
    @StepScope
    public HelloWorldProcessor processor() {
        return new HelloWorldProcessor();
    }

    /**
     * Writer 빈 - StepScope로 설정
     */
    @Bean
    @StepScope
    public HelloWorldWriter writer() {
        return new HelloWorldWriter();
    }
}
