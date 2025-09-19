package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingCriteria;
import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.loopers.domain.ranking.RankingPeriod;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller implements RankingV1Spec{

    private final RankingFacade rankingFacade;

    @GetMapping
    @Override
    public ApiResponse<List<RankingV1Dto.RankingResponse>> getRanking(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @RequestParam(defaultValue = "DAILY") String period
    ) {
        int zeroBasedPage = Math.max(page - 1, 0);
        RankingCriteria.Search criteria = new RankingCriteria.Search(zeroBasedPage, size, date, RankingPeriod.valueOf(period));
        Pageable pageable = PageRequest.of(zeroBasedPage, size);

        Page<RankingResult> rankingResults = rankingFacade.findRankings(criteria, pageable);
        List<RankingV1Dto.RankingResponse> responses = rankingResults.getContent()
                .stream()
                .map(RankingV1Dto.RankingResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }
}
