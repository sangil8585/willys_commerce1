package com.loopers.domain.user;

public class UserCommand {
    public record Create(
            String userId,
            UserEntity.Gender gender,
            String birthDate,
            String email
    ) {
        // 정적 펙토리 메서드..
        public static Create of(
                String userId,
                String gender,
                String birthDate,
                String email
        ) {
            return new Create(userId, UserEntity.Gender.from(gender), birthDate, email);
        }
    }
}
