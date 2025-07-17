package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;

public class UserTest {
    /**
     * 단위 테스트
     * 1. - ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.
     * 2. - 이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.
     * 3. - 생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.
     */

    @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @ParameterizedTest
    @ValueSource(strings = {
            "상일",
            "나는바보당히히",
            "asdf_!@312__",
            "abcdefghijk",
            "",
    })
    void ID가_형식이_안맞으면_생성실패(String userId) {
        // arrange
        final String name = "변상일";
        final String email = "asdf@gmail.com";
        final String birth = "1999-12-31";

        // act
        final CoreException exception = assertThrows(CoreException.class, () ->{
            new UserEntity(
                    userId,
                    name,
                    email,
                    birth
            );
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void 이메일이_형식에_안맞으면_생성실패() {
        // arrange
        final String userId = "sangil44";
        final String name = "변상일";
        final String email = "asdf@gmail";
        final String birth = "1999-12-31";

        // act
        final CoreException exception = assertThrows(CoreException.class, () ->{
            new UserEntity(
                    userId,
                    name,
                    email,
                    birth
            );
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void 생년월일이_형식에_안맞으면_생성실패() {
        // arrange
        final String userId = "sangil44";
        final String name = "변상일";
        final String email = "asdf@gmail.com";
        final String birth = "1993.02.24";

        // act
        final CoreException exception = assertThrows(CoreException.class, () ->{
            new UserEntity(
                    userId,
                    name,
                    email,
                    birth
            );
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
