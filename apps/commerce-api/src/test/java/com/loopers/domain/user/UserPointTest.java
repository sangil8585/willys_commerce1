package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class UserPointTest {

    @DisplayName("포인트 충전")
    @Nested
    class GetUserPointTest{

        /**
         * -[ ] 0 이하의 정수로 포인트를 충전 시 실패한다.
         */
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @Test
        void 포인트가_정수가_아닐시_충전_실패() {
            // given
            UserPointVO userPoint = new UserPointVO();
            long invalidAmount = -100;

            // when
            CoreException coreException = assertThrows(CoreException.class, () -> {
                userPoint.charge(invalidAmount);
            });

            // then
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
