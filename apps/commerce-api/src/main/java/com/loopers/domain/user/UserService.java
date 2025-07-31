package com.loopers.domain.user;

import com.loopers.domain.point.PointService;
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
    private final PointService pointService;

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

    @Transactional
    public void validateAndDeductPoints(Long userId, Long totalAmount) {
        // userId를 String으로 변환 (UserEntity의 userId는 String 타입)
        String userStringId = String.valueOf(userId);
        
        // 현재 포인트 조회
        Long currentPoints = pointService.get(userStringId);
        
        if (currentPoints == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자 포인트 정보를 찾을 수 없습니다.");
        }
        
        if (currentPoints < totalAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        
        // 포인트 차감 (음수로 charge하여 차감)
        pointService.charge(userStringId, -totalAmount);
    }
}
