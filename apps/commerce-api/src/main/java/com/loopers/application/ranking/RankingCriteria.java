package com.loopers.application.ranking;

import java.time.LocalDate;

public class RankingCriteria {
    public record Search(
            int page,
            int size,
            LocalDate date
    ) {

    }
}
