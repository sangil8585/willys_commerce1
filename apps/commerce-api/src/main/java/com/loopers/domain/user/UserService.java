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
    public UserEntity signUp(UserCommand.Create createCommand) {

        if(userRepository.existsUserId(createCommand.userId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 리소스입니다.");
        }

        // 유저entity -> userInfo
        UserEntity userEntity = UserEntity.from(createCommand);
        return userRepository.save(userEntity);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUserId(String userId) {

        // null을 방지하기위한 optional, userEntity를 매핑해준다.
        // X_USER_ID가 PK ID인지 로그인 ID인지 햇갈림..
        Optional<UserEntity> optional = userRepository.findByUserId(userId);

        return userRepository.findByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }
}
