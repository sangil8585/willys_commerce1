package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller {

    private final PointFacade pointFacade;

    @GetMapping
    public ApiResponse<PointV1Dto.PointResponse> getPointInfo(
            @RequestHeader("X-USER-ID") String userId
    ) {
        PointInfo pointInfo = pointFacade.getPointInfo(userId);

        return ApiResponse.success(new PointV1Dto.PointResponse(pointInfo.userId(), pointInfo.amount()));
    }

    @PostMapping("/charge")
    public ApiResponse<PointV1Dto.PointResponse> charge(
            @RequestBody PointV1Dto.PointRequest pointRequest
    ) {
        PointInfo pointInfo = pointFacade.chargePoint(pointRequest.userId(), pointRequest.point());
        return ApiResponse.success(new PointV1Dto.PointResponse(pointInfo.userId(), pointInfo.amount()));
    }


}
