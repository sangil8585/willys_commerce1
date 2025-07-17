package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserCommand;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
            @NotNull
            String userId,
            @NotNull
            String gender,
            @NotNull
            String birthDate,
            @NotNull
            String email
    ) {
        public UserCommand.Create toCommand() {
            return UserCommand.Create.of(
                    userId,
                    gender,
                    birthDate,
                    email
            );
        }
    }

    public record UserResponse(
            Long id,
            String userId,
            String gender,
            String birthDate,
            String email
    ) {

    }

}
