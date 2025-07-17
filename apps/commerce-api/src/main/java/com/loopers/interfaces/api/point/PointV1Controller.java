package com.loopers.interfaces.api.point;

import com.loopers.domain.user.PointService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final PointService pointService;

    @GetMapping
    public ApiResponse<PointV1Dto.PointResponse> getPointInfo(
            @RequestHeader("X-USER-ID") String userId
    ) {
        Long pointInfo = pointService.get(userId);

        return ApiResponse.success(new PointV1Dto.PointResponse(userId, pointInfo));
    }

    @PostMapping("/charge")
    public ApiResponse<PointV1Dto.PointResponse> charge(
            @RequestBody PointV1Dto.PointRequest pointRequest
    ) {
        Long pointInfo = pointService.charge(pointRequest.userId(), pointRequest.point());
        return ApiResponse.success(new PointV1Dto.PointResponse(pointRequest.userId(), pointInfo));
    }


}
