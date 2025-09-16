package com.loopers.application.ranking;

public record RankingResult(
        Long productId,
        String name,
        Long price,
        Long rank
) {

}
