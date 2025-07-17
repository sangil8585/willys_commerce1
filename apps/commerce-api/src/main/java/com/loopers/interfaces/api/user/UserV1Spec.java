package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API")
public interface UserV1Spec {

    @Operation(
            summary = "회원 가입",
            description = "회원 가입"
    )
    ApiResponse<UserV1Dto.UserResponse> signUp(
            @Schema(name = "회원가입 요청", description = "회원가입 요청 객체")
            UserV1Dto.SignUpRequest signUpRequest
    );
}
