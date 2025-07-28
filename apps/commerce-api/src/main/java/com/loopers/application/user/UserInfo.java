package com.loopers.application.user;

import com.loopers.domain.user.UserEntity;

public record UserInfo(
        Long id,
        String userId,
        UserEntity.Gender gender,
        String birthDate,
        String email
) {
    public static UserInfo from(com.loopers.domain.user.UserInfo domainUserInfo) {
        return new UserInfo(
                domainUserInfo.id(),
                domainUserInfo.userId(),
                domainUserInfo.gender(),
                domainUserInfo.birthDate(),
                domainUserInfo.email()
        );
    }
} 