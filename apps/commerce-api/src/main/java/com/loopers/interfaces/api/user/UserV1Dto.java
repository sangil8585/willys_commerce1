package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserInfo;
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
        public static UserV1Dto.UserResponse from(UserInfo info) {
            return new UserV1Dto.UserResponse(
                    info.id(),
                    info.userId(),
                    info.gender().name(),
                    info.birthDate(),
                    info.email()
            );
        }
    }

}
