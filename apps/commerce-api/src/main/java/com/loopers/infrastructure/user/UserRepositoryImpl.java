package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userJpaRepository.save(userEntity);
    }

    @Override
    public boolean existsUserId(String userId) {
        return userJpaRepository.existsByUserId(userId);
    }

    @Override
    public Optional<UserEntity> findByUserId(String userId) {
        return userJpaRepository.findByUserId(userId);
    }


}
