package com.loopers.application.user;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final PointRepository pointRepository;

    @Transactional
    public UserInfo signUp(UserCommand.Create createCommand) {
        UserEntity userEntity = userService.signUp(createCommand);
        pointRepository.createPointForUser(userEntity.getUserId());
        return UserInfo.from(userEntity);
    }

    @Transactional(readOnly = true)
    public UserInfo findByUserId(String userId) {

        UserEntity userEntity = userService.findByUserId(userId).orElseThrow(() ->
                new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다."));

        return UserInfo.from(userEntity);
    }
} 
