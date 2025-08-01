package com.loopers.application.user;

import com.loopers.domain.user.UserEntity;

public record UserInfo(
        Long id,
        String userId,
        UserEntity.Gender gender,
        String birthDate,
        String email
) {
    public static UserInfo from(UserEntity userEntity) {
        return new UserInfo(
                userEntity.getId(),
                userEntity.getUserId(),
                userEntity.getGender(),
                userEntity.getBirth(),
                userEntity.getEmail()
        );
    }
} 
