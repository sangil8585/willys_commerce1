package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1Spec{

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.SignUpRequest signUpRequest
    ) {
        if(signUpRequest.gender() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 입니다.");
        }

        return ApiResponse.success(
                new UserV1Dto.UserResponse(
                        1L,
                        "sangil8585",
                        UserV1Dto.UserResponse.GenderResponse.MALE,
                        "1993-02-24",
                        "sangil8585@naver.com"
                )
        );
    }
}
