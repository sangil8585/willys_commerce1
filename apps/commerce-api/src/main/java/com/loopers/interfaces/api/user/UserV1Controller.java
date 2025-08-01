package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/")
public class UserV1Controller implements UserV1Spec{

    private final UserFacade userFacade;

    @PostMapping("/users")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.SignUpRequest signUpRequest
    ) {
        if(signUpRequest.gender() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 입니다.");
        }

        UserCommand.Create command = signUpRequest.toCommand();
        UserInfo userInfo = userFacade.signUp(command);

        return ApiResponse.success(UserV1Dto.UserResponse.from(userInfo));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getMyInfo(
            @RequestHeader("X-USER-ID") String userId
    ) {
        UserInfo userInfo = userFacade.findByUserId(userId);

        if(userInfo == null) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(userInfo);

        return ApiResponse.success(response);
    }
}
