package com.loopers.interfaces.api.user;

import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
            @NotNull
            String userId,
            @NotNull
            String name,
            @NotNull
            GenderRequest gender,
            @NotNull
            String birthDate,
            @NotNull
            String email
    ) {
        enum GenderRequest {
            MALE,
            FEMALE
        }
    }

    public record UserResponse(
            String userId,
            String name,
            GenderResponse gender,
            String birthDate,
            String email
    ) {
    }

    enum GenderResponse {
        MALE,
        FEMALE
    }
}
