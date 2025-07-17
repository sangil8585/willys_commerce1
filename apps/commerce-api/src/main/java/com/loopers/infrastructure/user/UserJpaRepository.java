package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    // jpa가 동적으로 메서드를 구현해줌
    boolean existsByUserId(String userId);

    Optional<UserEntity> findByUserId(String userId);
}
