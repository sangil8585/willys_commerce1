package com.loopers.interfaces.api.point;

public class PointV1Dto {
    public record PointResponse(
            String userId,
            Long point
    ) {

    }

    public record PointRequest(
            String userId,
            Long point
    ) {

    }
}
