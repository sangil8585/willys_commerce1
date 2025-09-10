package com.loopers.infrastructure.ranking;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ranking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "order_count", nullable = false)
    private Long orderCount;

    public RankingEntity(Long productId, LocalDate date, Long orderCount) {
        this.productId = productId;
        this.date = date;
        this.orderCount = orderCount;
    }
}
