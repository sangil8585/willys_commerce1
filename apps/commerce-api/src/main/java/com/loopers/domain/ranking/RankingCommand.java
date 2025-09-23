package com.loopers.domain.ranking;

import java.time.LocalDate;

public class RankingCommand {
    public record Rankings(
            int size,
            int page,
            LocalDate date,
            RankingPeriod period
    ) {

    }

    public record ProductRank(
            LocalDate date,
            RankingPeriod period
    ) {

    }
}
