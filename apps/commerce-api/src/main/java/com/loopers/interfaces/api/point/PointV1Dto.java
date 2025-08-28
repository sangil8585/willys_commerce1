package com.loopers.interfaces.api.point;

public class PointV1Dto {
    public record PointResponse(
            Long userId,
            Long point
    ) {

    }

    public record PointRequest(
            Long userId,
            Long point
    ) {

    }
}
