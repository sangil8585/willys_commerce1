package com.loopers.batch.domain.rollup;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "mv_product_rank_monthly",
        indexes = {
                @Index(name = "idx_mv_monthly_as_of_date", columnList = "as_of_date"),
                @Index(name = "idx_mv_monthly_as_of_date_rank", columnList = "as_of_date, rank"),
                @Index(name = "idx_mv_monthly_as_of_date_product", columnList = "as_of_date, product_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_mv_monthly_as_of_product", columnNames = {"as_of_date", "product_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRankMonthlyEntity extends BaseEntity {

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate; // 윈도우 종료 기준일

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Transient
    private Integer score; // 가중치 점수(비영속)

    protected ProductRankMonthlyEntity(LocalDate asOfDate, Long productId, Integer orderCount, Integer likeCount, Integer viewCount, Integer rank) {
        this.asOfDate = asOfDate;
        this.productId = productId;
        this.orderCount = orderCount;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.rank = rank;
    }

    public static ProductRankMonthlyEntity of(LocalDate asOfDate, Long productId, Integer orderCount, Integer likeCount, Integer viewCount, Integer rank) {
        return new ProductRankMonthlyEntity(asOfDate, productId, orderCount, likeCount, viewCount, rank);
    }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public void setRank(Integer rank) { this.rank = rank; }
}


