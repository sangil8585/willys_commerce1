package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/")
public class UserV1Controller implements UserV1Spec{

    private final UserService userService;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.SignUpRequest signUpRequest
    ) {
        if(signUpRequest.gender() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 입니다.");
        }

        UserCommand.Create command = signUpRequest.toCommand();
        UserInfo userInfo = userService.signUp(command);

        return ApiResponse.success(
                // Mock API
                new UserV1Dto.UserResponse(
                        1L,
                        "sangil8585",
                        "MALE",
                        "1993-02-24",
                        "sangil8585@naver.com"
                )
        );
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getMyInfo(
            @RequestHeader("X-USER-ID") String userId
    ) {
        UserInfo userInfo = userService.findByUserId(userId);

        if(userInfo == null) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(userInfo);

        return ApiResponse.success(response);
    }
}
