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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1Controller implements RankingV1Spec{

    private final RankingFacade rankingFacade;

    @GetMapping
    @Override
    public ApiResponse<List<RankingV1Dto.RankingResponse>> getRanking(
            @RequestBody RankingV1Dto.RankingRequest request
    ) {
        RankingCriteria.Search criteria = new RankingCriteria.Search(
            request.page(), 
            request.size(), 
            request.date()
        );
        Pageable pageable = PageRequest.of(request.page(), request.size());
        
        Page<RankingResult> rankingResults = rankingFacade.findRankings(criteria, pageable);
        List<RankingV1Dto.RankingResponse> responses = rankingResults.getContent()
                .stream()
                .map(RankingV1Dto.RankingResponse::from)
                .toList();
        
        return ApiResponse.success(responses);
    }
}
