package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingCriteria;
import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1Controller {

    private final RankingFacade rankingFacade;

    @GetMapping("")
    public ApiResponse<List<RankingV1Dto.RankingResponse>> getRanking(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) LocalDate date
    ) {
        
        RankingCriteria.Search criteria = new RankingCriteria.Search(page, size, date);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<RankingResult> rankingResults = rankingFacade.findRankings(criteria, pageable);
        List<RankingV1Dto.RankingResponse> responses = rankingResults.getContent()
                .stream()
                .map(RankingV1Dto.RankingResponse::from)
                .toList();
        
        return ApiResponse.success(responses);
    }
}
