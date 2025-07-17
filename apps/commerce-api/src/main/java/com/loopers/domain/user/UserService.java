package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserInfo signUp(UserCommand.Create createCommand) {

        if(userRepository.existsUserId(createCommand.userId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 리소스입니다.");
        }

        // 유저entity -> userInfo
        UserEntity userEntity = UserEntity.from(createCommand);
        UserEntity saved = userRepository.save(userEntity);
        return UserInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public UserInfo findByUserId(String userId) {

        // null을 방지하기위한 optional, userEntity를 매핑해준다.
        // X_USER_ID가 PK ID인지 로그인 ID인지 햇갈림..
        Optional<UserEntity> optional = userRepository.findByUserId(userId);

        return optional.isPresent() ? UserInfo.from(optional.get()) : null;
    }
}
