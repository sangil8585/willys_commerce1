package com.loopers.domain.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PointServiceIntegrationTest {
    @Autowired
    private PointFacade pointFacade;
    @Autowired
    private UserFacade userFacade;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /**
     * - [x]  해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.
     * - [ ]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     *
     */
    @DisplayName("포인트조회")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void 회원이_존재할경우_보유포인트_반환() {
            // given
            UserCommand.Create createCommand = new UserCommand.Create(
                    "sangil8585",
                    UserEntity.Gender.MALE,
                    "1993-02-24",
                    "asdfas@naver.com"
            );

            UserInfo result = userFacade.signUp(createCommand);

            // when
            PointInfo pointInfo = pointFacade.getPointInfo(result.userId());

            // then
            assertNotNull(pointInfo);
            assertNotNull(pointInfo.amount());
            assertTrue(!(pointInfo.amount() < 0));
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void 아이디가_존재하지않는_경우_null반환() {
            // given
            String nonExist = "nonExist";

            // when
            PointInfo pointInfo = pointFacade.getPointInfo(nonExist);

            // then
            assertNull(pointInfo);
        }
    }

    /**
     * - [ ]  존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.
     */
    @DisplayName("포인트충전")
    @Nested
    class Charge {
        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void 존재하지_않는_유저로_충전시_실패() {
            // given
            String nonExist = "nonExist";
            Long amount = 100L;

            // when

            CoreException coreException = assertThrows(CoreException.class, () -> {
                pointFacade.chargePoint(nonExist, amount);
            });

            // then
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
} 