package com.loopers.batch.job.rollup;

import com.loopers.batch.domain.rollup.ProductRankMonthlyEntity;
import com.loopers.batch.domain.rollup.ProductRankWeeklyEntity;
import com.loopers.batch.infrastructure.rollup.ProductRankMonthlyJpaRepository;
import com.loopers.batch.infrastructure.rollup.ProductRankWeeklyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RollupJobConfig {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;
    private final ProductRankWeeklyJpaRepository weeklyRepo;
    private final ProductRankMonthlyJpaRepository monthlyRepo;

    @Bean
    public Job slidingRollupJob(Step weeklyRollupStep, Step monthlyRollupStep) {
        return new JobBuilder("slidingRollupJob", jobRepository)
                .start(weeklyRollupStep)
                .next(monthlyRollupStep)
                .build();
    }

    @Bean
    public Step weeklyRollupStep(ListItemReader<ProductRankWeeklyEntity> weeklyReader,
                                 ItemProcessor<ProductRankWeeklyEntity, ProductRankWeeklyEntity> weeklyProcessor) {
        return new StepBuilder("weeklyRollupStep", jobRepository)
                .<ProductRankWeeklyEntity, ProductRankWeeklyEntity>chunk(1000, transactionManager)
                .reader(weeklyReader)
                .processor(weeklyProcessor)
                .writer(items -> {
                    if (items.isEmpty()) return;
                    LocalDate asOfDate = items.getItems().get(0).getAsOfDate();
                    weeklyRepo.deleteByAsOfDate(asOfDate);
                    weeklyRepo.saveAll(items.getItems());
                })
                .build();
    }

    @Bean
    public Step monthlyRollupStep(ListItemReader<ProductRankMonthlyEntity> monthlyReader,
                                  ItemProcessor<ProductRankMonthlyEntity, ProductRankMonthlyEntity> monthlyProcessor) {
        return new StepBuilder("monthlyRollupStep", jobRepository)
                .<ProductRankMonthlyEntity, ProductRankMonthlyEntity>chunk(1000, transactionManager)
                .reader(monthlyReader)
                .processor(monthlyProcessor)
                .writer(items -> {
                    if (items.isEmpty()) return;
                    LocalDate asOfDate = items.getItems().get(0).getAsOfDate();
                    monthlyRepo.deleteByAsOfDate(asOfDate);
                    monthlyRepo.saveAll(items.getItems());
                })
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<ProductRankWeeklyEntity> weeklyReader(@Value("#{jobParameters['asOfDate']}") String asOfDateStr) {
        LocalDate asOfDate = LocalDate.parse(asOfDateStr, DATE_FMT);
        LocalDate start = asOfDate.minusDays(6);

        // 집계 쿼리: 최근 7일 product_metrics 합산, 정렬 후 TOP 100
        String sql = """
            SELECT product_id,
                   SUM(order_count) AS order_count,
                   SUM(like_count)  AS like_count,
                   SUM(view_count)  AS view_count
            FROM product_metrics
            WHERE date BETWEEN ? AND ?
            GROUP BY product_id
            ORDER BY (SUM(order_count)*7 + SUM(like_count)*2 + SUM(view_count)*1) DESC,
                     order_count DESC, like_count DESC, view_count DESC
            LIMIT 100
        """;

        List<ProductRankWeeklyEntity> rows = jdbcTemplate.query(sql, (rs, rowNum) ->
                ProductRankWeeklyEntity.of(
                        asOfDate,
                        rs.getLong("product_id"),
                        rs.getInt("order_count"),
                        rs.getInt("like_count"),
                        rs.getInt("view_count"),
                        rowNum + 1
                ), start, asOfDate);

        return new ListItemReader<>(rows);
    }

    @Bean
    @StepScope
    public ItemProcessor<ProductRankWeeklyEntity, ProductRankWeeklyEntity> weeklyProcessor() {
        return item -> {
            int score = item.getOrderCount() * 7 + item.getLikeCount() * 2 + item.getViewCount() * 1;
            item.setScore(score);
            return item;
        };
    }

    @Bean
    @StepScope
    public ListItemReader<ProductRankMonthlyEntity> monthlyReader(@Value("#{jobParameters['asOfDate']}") String asOfDateStr) {
        LocalDate asOfDate = LocalDate.parse(asOfDateStr, DATE_FMT);
        LocalDate start = asOfDate.minusDays(29);

        String sql = """
            SELECT product_id,
                   SUM(order_count) AS order_count,
                   SUM(like_count)  AS like_count,
                   SUM(view_count)  AS view_count
            FROM product_metrics
            WHERE date BETWEEN ? AND ?
            GROUP BY product_id
            ORDER BY (SUM(order_count)*7 + SUM(like_count)*2 + SUM(view_count)*1) DESC,
                     order_count DESC, like_count DESC, view_count DESC
            LIMIT 100
        """;

        List<ProductRankMonthlyEntity> rows = jdbcTemplate.query(sql, (rs, rowNum) ->
                ProductRankMonthlyEntity.of(
                        asOfDate,
                        rs.getLong("product_id"),
                        rs.getInt("order_count"),
                        rs.getInt("like_count"),
                        rs.getInt("view_count"),
                        rowNum + 1
                ), start, asOfDate);

        return new ListItemReader<>(rows);
    }

    @Bean
    @StepScope
    public ItemProcessor<ProductRankMonthlyEntity, ProductRankMonthlyEntity> monthlyProcessor() {
        return item -> {
            int score = item.getOrderCount() * 7 + item.getLikeCount() * 2 + item.getViewCount() * 1;
            item.setScore(score);
            return item;
        };
    }
}


