package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "member")
public class UserEntity extends BaseEntity {
    private static final String USER_ID_REGEX = "^[a-zA-Z0-9]{1,10}$";
    private static final String USER_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String USER_BIRTH_REGEX = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";

    private String userId;
    private Gender gender;
    private String birth;
    private String email;

    public static UserEntity of(UserCommand.Create command){
        return new UserEntity(command.userId(), command.gender(), command.birthDate(), command.email());
    }

    protected UserEntity() {}

    UserEntity(String userId, Gender gender, String birth, String email) {
        super();
        if (!userId.matches(USER_ID_REGEX)) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "ID는 영문 및 숫자 10자 이내 형식에 맞춰주세요."
            );
        }
        
        if (!birth.matches(USER_BIRTH_REGEX)) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "생년월일이 `yyyy-MM-dd` 형식에 맞지 않습니다."
            );
        }
        if (!email.matches(USER_EMAIL_REGEX)) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "이메일이 `xx@yy.zz` 형식에 맞지 않습니다."
            );
        }
        this.userId = userId;
        this.gender = gender;
        this.birth = birth;
        this.email = email;
    }

    public static UserEntity from(UserCommand.Create command) {
        return new UserEntity(command.userId(), command.gender(), command.birthDate(), command.email());
    }

    public enum Gender {
        MALE, FEMALE;

        public static Gender from(String gender) {
            return switch (gender) {
                case "MALE" -> MALE;
                case "FEMALE" -> FEMALE;
                default -> throw new CoreException(ErrorType.BAD_REQUEST);
            };
        }
    }
}
