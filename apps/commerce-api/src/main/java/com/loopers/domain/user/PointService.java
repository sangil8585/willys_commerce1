package com.loopers.domain.user;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointService {
    private final UserRepository userRepository;

    public Long get(String userId) {
        Optional<UserEntity> optional = userRepository.findByUserId(userId);
        if(optional.isEmpty()) {
            return null;
        }
        UserEntity user = optional.get();
        return user.getUserPointVO().getAmount();
    }

    public Long charge(String userId, Long amount) {
        Optional<UserEntity> optional = userRepository.findByUserId(userId);
        if(optional.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        UserEntity user = optional.get();
        Long result = user.charge(amount);
        userRepository.save(user);

        return result;
    }
}
