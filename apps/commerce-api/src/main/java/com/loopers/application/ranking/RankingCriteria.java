package com.loopers.application.ranking;

import java.time.LocalDate;
import com.loopers.domain.ranking.RankingPeriod;

public class RankingCriteria {
    public record Search(
            int page,
            int size,
            LocalDate date,
            RankingPeriod period
    ) {

    }
}
