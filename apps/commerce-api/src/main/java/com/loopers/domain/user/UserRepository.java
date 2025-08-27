package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity user);

    boolean existsUserId(Long userId);

    Optional<UserEntity> findByUserId(Long userId);

    boolean existsById(Long userId);
    
    Optional<UserEntity> findById(Long id);
}
