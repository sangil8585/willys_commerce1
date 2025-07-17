package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

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

    @Operation(
            summary = "내 정보 조회",
            description = "ID로 회원 정보를 조회합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> getMyInfo(
            @Schema(name = "예시 ID", description = "조회할 예시의 ID")
            @RequestHeader("X-USER-ID") String userId
    );
}
