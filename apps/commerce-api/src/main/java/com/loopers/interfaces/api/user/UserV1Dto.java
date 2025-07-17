package com.loopers.interfaces.api.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
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
            Long userId,
            String loginId,
            GenderResponse gender,
            String birthDate,
            String email
    ) {
        enum GenderResponse {
            MALE,
            FEMALE
        }
    }
}
