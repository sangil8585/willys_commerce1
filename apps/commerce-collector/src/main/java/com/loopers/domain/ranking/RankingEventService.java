package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingEventService {

    private final RankingCacheService rankingCacheService;
    private final WeightService weightService;

    public void handleProductView(Long productId) {
        double score = weightService.getViewWeight() * 1.0;
        rankingCacheService.updateScore(productId, score, LocalDate.now());
    }

    public void handleLike(Long productId, boolean isCreated) {
        double weight = isCreated ? weightService.getLikeWeight() : -weightService.getLikeWeight();
        double score = weight * 1.0;
        rankingCacheService.updateScore(productId, score, LocalDate.now());
    }

    public void handleOrder(Long productId, BigDecimal price, Integer quantity) {
        double orderAmount = price.multiply(BigDecimal.valueOf(quantity)).doubleValue();
        double score = weightService.getOrderWeight() * orderAmount;
        rankingCacheService.updateScore(productId, score, LocalDate.now());
    }
}
