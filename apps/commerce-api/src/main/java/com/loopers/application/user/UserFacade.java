package com.loopers.application.user;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final PointRepository pointRepository;

    @Transactional
    public UserInfo signUp(UserCommand.Create createCommand) {
        com.loopers.domain.user.UserInfo domainUserInfo = userService.signUp(createCommand);
        pointRepository.createPointForUser(domainUserInfo.userId());
        return UserInfo.from(domainUserInfo);
    }

    @Transactional(readOnly = true)
    public UserInfo findByUserId(String userId) {
        com.loopers.domain.user.UserInfo domainUserInfo = userService.findByUserId(userId);
        if (domainUserInfo == null) {
            return null;
        }
        return UserInfo.from(domainUserInfo);
    }
} 