package com.loopers.batch.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(properties = {
        "spring.testcontainers.enabled=false",
        "spring.docker.compose.enabled=false",
        "spring.main.web-application-type=none"
})
@ActiveProfiles("local")
class MetricAggregationJobConfigTest {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private Job slidingRollupJob;

    private LocalDate asOfDate;

    @BeforeEach
    void setUp() {
        asOfDate = LocalDate.now().minusDays(1);

        jdbcTemplate.update("DELETE FROM product_metrics WHERE date BETWEEN ? AND ?",
                asOfDate.minusDays(29), asOfDate);
        jdbcTemplate.update("DELETE FROM mv_product_rank_weekly WHERE as_of_date = ?", asOfDate);
        jdbcTemplate.update("DELETE FROM mv_product_rank_monthly WHERE as_of_date = ?", asOfDate);


        LocalDate d1 = asOfDate;
        LocalDate d2 = asOfDate.minusDays(1);


        jdbcTemplate.update("INSERT INTO product_metrics (product_id, date, like_count, order_count, view_count) VALUES (?,?,?,?,?)",
                1001L, d1, 7, 10, 20);
        jdbcTemplate.update("INSERT INTO product_metrics (product_id, date, like_count, order_count, view_count) VALUES (?,?,?,?,?)",
                1001L, d2, 5, 8,  15);
        jdbcTemplate.update("INSERT INTO product_metrics (product_id, date, like_count, order_count, view_count) VALUES (?,?,?,?,?)",
                1002L, d1, 3, 4, 10);
        jdbcTemplate.update("INSERT INTO product_metrics (product_id, date, like_count, order_count, view_count) VALUES (?,?,?,?,?)",
                1002L, d2, 2, 3, 8);
    }

    @Test
    void metricAggregationJob_runs_successfully() throws Exception {
        jobLauncherTestUtils.setJob(slidingRollupJob);
        JobParameters params = new JobParametersBuilder()
                .addString("asOfDate", asOfDate.format(DATE_FMT))
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Long weekly = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly WHERE as_of_date = ?",
                Long.class, asOfDate);
        Long monthly = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_monthly WHERE as_of_date = ?",
                Long.class, asOfDate);

        assertThat(weekly).isNotNull();
        assertThat(monthly).isNotNull();
        assertThat(weekly).isGreaterThan(0L);
        assertThat(monthly).isGreaterThan(0L);

        Long weeklyTop1 = jdbcTemplate.queryForObject(
                "SELECT product_id FROM mv_product_rank_weekly WHERE as_of_date = ? ORDER BY rank ASC LIMIT 1",
                Long.class, asOfDate);
        Long monthlyTop1 = jdbcTemplate.queryForObject(
                "SELECT product_id FROM mv_product_rank_monthly WHERE as_of_date = ? ORDER BY rank ASC LIMIT 1",
                Long.class, asOfDate);

        assertThat(weeklyTop1).isEqualTo(1001L);
        assertThat(monthlyTop1).isEqualTo(1001L);
    }

    @Test
    void weeklyStep_runs_successfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("asOfDate", asOfDate.format(DATE_FMT))
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("weeklyRollupStep", params);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Long weekly = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly WHERE as_of_date = ?",
                Long.class, asOfDate);
        assertThat(weekly).isNotNull();
        assertThat(weekly).isGreaterThan(0L);
    }
}


