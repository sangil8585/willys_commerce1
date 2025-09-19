package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Ranking V1 API", description = "랭킹 관련 API 입니다.")
public interface RankingV1Spec {
    @Operation(
            summary = "랭킹 목록 조회",
            description = "랭킹 목록을 조회합니다."
    )
    ApiResponse<List<RankingV1Dto.RankingResponse>> getRanking(
        @Parameter(description = "페이지 번호 (1부터 시작)") int page,
        @Parameter(description = "페이지 크기") int size,
        @Parameter(description = "조회 날짜 - yyyyMMdd") LocalDate date,
        @Parameter(description = "기간(DAILY, WEEKLY, MONTHLY)") String period
    );
}
