package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderV1ApiE2ETest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private static final String ENDPOINT = "/api/v1/orders";

    @DisplayName("주문 요청 성공시 주문 정보를 응답으로 반환")
    @Test
    void 주문_요청성공시_주문정보_반환() {
        // given


        //when
//        ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderResponse>>
//                responseType = new ParameterizedTypeReference<>() {};
//        ResponseEntity<ApiResponse<OrderV1Dto>> response =
//                testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(null), responseType);

        //then
//        assertAll(
//                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
//                () -> assertThat(response.getBody()).isNotNull(),
//                () -> assertThat(response.getBody().data().),
//        );
    }
}
