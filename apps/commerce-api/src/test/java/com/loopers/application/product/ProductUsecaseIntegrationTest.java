package com.loopers.application.product;

import com.loopers.utils.DatabaseCleanUp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProductUsecaseIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    // 인수(인계할때..)와 회귀(오류)방지.
    /**
     * - [] 유저는 상품조회가 가능하다.
     */
    @DisplayName("상품조회")
    @Nested
    class getProduct {
        @DisplayName("상품 목록 조회시 목록이 반환된다.")
        @Test
        void 상품목록_조회시_목록이_반환된다() {
            // given

            // when

            // then
        }
    }


}
