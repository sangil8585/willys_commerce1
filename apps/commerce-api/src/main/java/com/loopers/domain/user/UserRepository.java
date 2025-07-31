package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity user);

    boolean existsUserId(String userId);

    Optional<UserEntity> findByUserId(String userId);

    boolean existsById(Long userId);
}
