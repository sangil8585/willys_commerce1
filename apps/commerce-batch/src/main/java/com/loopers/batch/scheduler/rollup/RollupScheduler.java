package com.loopers.batch.scheduler.rollup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RollupScheduler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JobLauncher jobLauncher;
    private final Job slidingRollupJob;

    // 매일 00:20 슬라이딩 주/월 롤업 실행
    @Scheduled(cron = "0 20 0 * * *", zone = "Asia/Seoul")
    public void runRollup() throws Exception {
        String asOfDate = LocalDate.now().minusDays(1).format(DATE_FMT);
        JobParameters params = new JobParametersBuilder()
                .addString("asOfDate", asOfDate)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.info("Starting slidingRollupJob for asOfDate={}", asOfDate);
        jobLauncher.run(slidingRollupJob, params);
    }
}


