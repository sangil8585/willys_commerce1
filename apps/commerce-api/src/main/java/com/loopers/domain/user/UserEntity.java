package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class UserEntity {
    private static final String USER_ID_REGEX = "^[a-zA-Z0-9]{1,10}$";
    private static final String USER_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String USER_BIRTH_REGEX = "^[a-zA-Z0-9]{1,10}$";

    private String userId;
    private String name;
    private String email;
    private String birth;

    UserEntity(String userId, String name, String email, String birth) {
        if (!userId.matches(USER_ID_REGEX)) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "ID는 영문 및 숫자 10자 이내 형식에 맞춰주세요."
            );
        }
        if (!email.matches(USER_EMAIL_REGEX)) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "이메일이 `xx@yy.zz` 형식에 맞지 않습니다."
            );
        }
        if (!email.matches(USER_BIRTH_REGEX)) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "생년월일이 `yyyy-MM-dd` 형식에 맞지 않습니다."
            );
        }
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.birth = birth;
    }
}
