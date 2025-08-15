package com.loopers.domain.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class UserServiceIntegrationTest {
    @MockitoSpyBean
    private UserRepository userRepository;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /**
     * - [x]  회원 가입시 User 저장이 수행된다. ( spy 검증 )
     * - [x]  이미 가입된 ID 로 회원가입 시도 시, 실패한다.
     */
    @DisplayName("회원가입")
    @Nested
    class SignUp {
        @DisplayName("회원 가입시 User 저장이 수행된다. ( spy 검증 )")
        @Test
        void 회원가입시_유저가_저장된다() {
            // given
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );

            // when
            UserInfo result = userFacade.signUp(createCommand);

            // then
            verify(userRepository).save(any(UserEntity.class));
            assertNotNull(result);
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다")
        @Test
        void 중복회원_가입시도시_실패한다() {
            // given
            // createCommand를 두번 signUp
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );

            userFacade.signUp(createCommand);

            // when
            // assertThrows 자체가 exception을 반환해준다
            CoreException coreException = assertThrows(CoreException.class, () ->
                    userFacade.signUp(createCommand));

            // then
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    /**
     * - [x]  해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.
     * - [ ]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     */
    @DisplayName("내 정보 조회")
    @Nested
    class findUserInfo {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void 아이디가_존재하면_유저정보_반환() {
            // given
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );
            userFacade.signUp(createCommand);

            // when
            UserInfo result = userFacade.findByUserId("sangil8585");

            // then
            assertEquals(createCommand.userId(), result.userId());
            assertEquals(createCommand.gender(), result.gender());
            assertEquals(createCommand.birthDate(), result.birthDate());
            assertEquals(createCommand.email(), result.email());
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, CoreException이 발생한다.")
        @Test
        void 아이디가_존재하지않으면_CoreException_발생() {
            // given
            String nonExist = "nonExist";

            // when & then
            CoreException coreException = assertThrows(CoreException.class, () ->
                    userFacade.findByUserId(nonExist));

            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(coreException.getMessage()).isEqualTo("존재하지 않는 사용자입니다.");
        }
    }

}
