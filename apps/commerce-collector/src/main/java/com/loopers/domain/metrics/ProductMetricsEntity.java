package com.loopers.domain.metrics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_metrics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetricsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ProductMetricsEntity(Long productId, LocalDate date, Integer likeCount, 
                               Integer orderCount, Integer orderQuantity, Integer viewCount) {
        this.productId = productId;
        this.date = date;
        this.likeCount = likeCount != null ? likeCount : 0;
        this.orderCount = orderCount != null ? orderCount : 0;
        this.orderQuantity = orderQuantity != null ? orderQuantity : 0;
        this.viewCount = viewCount != null ? viewCount : 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addLikeCount(int count) {
        this.likeCount += count;
        this.updatedAt = LocalDateTime.now();
    }

    public void addOrderCount(int count) {
        this.orderCount += count;
        this.updatedAt = LocalDateTime.now();
    }

    public void addOrderQuantity(int quantity) {
        this.orderQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void addViewCount(int count) {
        this.viewCount += count;
        this.updatedAt = LocalDateTime.now();
    }
}
