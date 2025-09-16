package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingResult;

public class RankingV1Dto {
    public record RankingResponse(
            Long productId,
            String name,
            Long price,
            Long rank
    ) {
        public static RankingResponse from(RankingResult result) {
            return new RankingResponse(
                    result.productId(),
                    result.name(),
                    result.price(),
                    result.rank()
            );
        }
    }
}
